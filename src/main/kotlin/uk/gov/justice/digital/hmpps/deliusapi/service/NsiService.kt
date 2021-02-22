package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ReferenceDataMasterRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TransferReasonRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCodeOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCodeOrThrow
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCrnOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.assertSupportedLevel
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getEvent
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getRequirement
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStaffOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getTeamOrBadRequest
import java.time.LocalDate

@Service
class NsiService(
  private val nsiRepository: NsiRepository,
  private val offenderRepository: OffenderRepository,
  private val nsiTypeRepository: NsiTypeRepository,
  private val providerRepository: ProviderRepository,
  private val transferReasonRepository: TransferReasonRepository,
  private val referenceDataMasterRepository: ReferenceDataMasterRepository,
) {
  @Auditable(AuditableInteraction.ADMINISTER_NSI)
  fun createNsi(request: NewNsi): NsiDto {
    val active = request.endDate == null
    val offender = offenderRepository.findByCrnOrBadRequest(request.offenderCrn)

    val audit = AuditContext.get(AuditableInteraction.ADMINISTER_NSI)
    audit.offenderId = offender.id

    val type = nsiTypeRepository.findByCodeOrBadRequest(request.type)
    type.assertSupportedLevel(request)

    if (active && !type.allowActiveDuplicates || !active && !type.allowInactiveDuplicates) {
      throw BadRequestException(
        "NSI type '${request.type}' does not allow ${if (active) "active" else "inactive"} duplicates & duplicate checking is not yet implemented"
      )
    }

    val subType = if (request.subType == null) null
    else type.subTypes?.find { it.code == request.subType }
      ?: throw BadRequestException("NSI sub type '${request.subType}' is not a valid sub type of '${type.code}'")

    if (subType == null && type.subTypes?.isEmpty() != true) {
      throw BadRequestException("NSI type '${type.code}' requires a sub type")
    }

    val status = type.statuses?.find { it.code == request.status }
      ?: throw BadRequestException("NSI status '${request.status}' is not a valid status of NSI type '${type.code}'")

    val intendedProvider = type.providers?.find { it.code == request.intendedProvider }
      ?: throw BadRequestException("Intended provider '${request.intendedProvider}' is not a valid provider of NSI type '${type.code}'")

    val outcome = if (request.outcome == null) null
    else type.outcomes?.find { it.code == request.outcome }
      ?: throw BadRequestException("Outcome type '${request.outcome}' is not a valid outcome type of '${type.code}'")

    if (type.units == null) {
      if (request.length != null) {
        throw BadRequestException("NSI type '${type.code}' does not support a length")
      }
    } else {
      if (request.length == null) {
        throw BadRequestException("NSI type '${type.code}' requires a length in units ${type.units?.code}")
      }

      if (request.length < type.minimumLength ?: Long.MIN_VALUE || request.length > type.maximumLength ?: Long.MAX_VALUE) {
        throw BadRequestException("NSI type '${type.code}' requires a length between ${type.minimumLength} & ${type.maximumLength} ${type.units?.code}")
      }
    }

    val event = offender.getEvent(request.eventId)
    val requirement = offender.getRequirement(event, request.requirementId)

    if (event != null && request.referralDate.isBefore(event.referralDate)) {
      throw BadRequestException("Referral date must not be before the event referral date '${event.referralDate}'")
    }

    if (requirement?.terminationDate != null) {
      if (active) {
        throw BadRequestException("End date is required as requirement has termination date '${requirement.terminationDate}'")
      }

      if (request.endDate?.isBefore(requirement.terminationDate) == true) {
        throw BadRequestException("End date must not be before the requirement termination date '${requirement.terminationDate}'")
      }
    }

    val nsi = Nsi(
      offender = offender,
      event = event,
      type = type,
      subType = subType,
      length = request.length,
      referralDate = request.referralDate,
      expectedStartDate = request.expectedStartDate,
      expectedEndDate = request.expectedEndDate,
      startDate = request.startDate,
      endDate = request.endDate,
      status = status,
      statusDate = request.statusDate,
      notes = request.notes,
      outcome = outcome,
      requirement = requirement,
      intendedProvider = intendedProvider,
      managers = listOf(createNsiManager(request.manager, request.referralDate)),
      active = active,
      pendingTransfer = false,
    )

    val entity = nsiRepository.saveAndFlush(nsi)

    audit.nsiId = entity.id

    return NsiMapper.INSTANCE.toDto(entity)
  }

  private fun createNsiManager(request: NewNsiManager, startDate: LocalDate): NsiManager {
    val provider = providerRepository.findByCode(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val team = provider.getTeamOrBadRequest(request.team)
    val staff = team.getStaffOrBadRequest(request.staff)

    return NsiManager(
      startDate = startDate,
      provider = provider,
      team = team,
      staff = staff,
      transferReason = transferReasonRepository.findByCode("NSI"),
      allocationReason = referenceDataMasterRepository.findByCodeOrThrow("NM ALLOCATION REASON")
        .standardReferences?.find { it.code == "IN1" },
    )
  }
}
