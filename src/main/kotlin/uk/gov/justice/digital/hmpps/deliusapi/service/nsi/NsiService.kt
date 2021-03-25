package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.config.Authorities
import uk.gov.justice.digital.hmpps.deliusapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.extensions.findByIdOrNotFound
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCodeOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCrnOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getEventOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getRequirementOrBadRequest

@Service
class NsiService(
  private val nsiRepository: NsiRepository,
  private val offenderRepository: OffenderRepository,
  private val nsiTypeRepository: NsiTypeRepository,
  private val mapper: NsiMapper,
  private val features: FeatureFlags,
  private val validation: NsiValidationService,
  private val nsiManagerService: NsiManagerService,
  private val nsiSystemContactService: NsiSystemContactService,
) {

  fun getUpdateNsi(id: Long): UpdateNsi = mapper.toUpdate(getNsi(id))

  @Transactional
  @Auditable(AuditableInteraction.ADMINISTER_NSI)
  fun updateNsi(id: Long, request: UpdateNsi): NsiDto {
    val entity = getNsi(id)

    if (request.statusDate < entity.statusDate) {
      throw BadRequestException("Updated status date must be equal to or after existing status date '${entity.statusDate}'")
    }

    if (entity.requirement != null) {
      validation.assertRequirementConstraints(entity.type, entity.requirement!!, request)
    }

    validation.assertTypeConstraints(entity.type, request)
    val newStatus = validation.validateStatus(entity.type, request)
    val newOutcome = validation.validateOutcome(entity.type, request)

    nsiSystemContactService.updateStatusContact(entity, newStatus, request)
    nsiSystemContactService.updateCommencedContact(entity, request)
    nsiSystemContactService.updateTerminationContact(entity, newOutcome, request)

    entity.apply {
      startDate = request.startDate
      endDate = request.endDate
      active = request.endDate == null
      expectedStartDate = request.expectedStartDate
      expectedEndDate = request.expectedEndDate
      length = request.length
      status = newStatus
      statusDate = request.statusDate
      outcome = newOutcome
      notes = listOfNotNull(entity.notes, request.notes).joinToString("\n")
    }

    nsiManagerService.updateNsiManager(entity, request.manager)

    validation.assertSupportedTypeLevel(entity)

    val audit = AuditContext.get(AuditableInteraction.ADMINISTER_NSI)
    audit.nsiId = entity.id

    nsiRepository.saveAndFlush(entity)

    return mapper.toDto(entity)
  }

  @PreAuthorize(
    "hasAuthority('${Authorities.PROVIDER}'.concat(#request.intendedProvider)) " +
      "and hasAuthority('${Authorities.PROVIDER}'.concat(#request.manager.provider))"
  )
  @Auditable(AuditableInteraction.ADMINISTER_NSI)
  @Transactional
  fun createNsi(request: NewNsi): NsiDto {
    val offender = offenderRepository.findByCrnOrBadRequest(request.offenderCrn)

    val audit = AuditContext.get(AuditableInteraction.ADMINISTER_NSI)
    audit.offenderId = offender.id

    val type = nsiTypeRepository.findByCodeOrBadRequest(request.type)

    val subType = if (request.subType == null) null
    else type.subTypes?.find { it.code == request.subType }
      ?: throw BadRequestException("NSI sub type '${request.subType}' is not a valid sub type of '${type.code}'")

    if (subType == null && type.subTypes?.isEmpty() != true) {
      throw BadRequestException("NSI type '${type.code}' requires a sub type")
    }

    val intendedProvider = type.providers?.find { it.code == request.intendedProvider }
      ?: throw BadRequestException("Intended provider '${request.intendedProvider}' is not a valid provider of NSI type '${type.code}'")

    val event = if (request.eventId == null) null else offender.getEventOrBadRequest(request.eventId)
    val requirement = if (event == null || request.requirementId == null) null
    else offender.getRequirementOrBadRequest(event, request.requirementId)

    if (event != null && request.referralDate.isBefore(event.referralDate)) {
      throw BadRequestException("Referral date must not be before the event referral date '${event.referralDate}'")
    }

    validation.assertTypeConstraints(type, request)
    val status = validation.validateStatus(type, request)
    val outcome = validation.validateOutcome(type, request)

    if (requirement != null) {
      validation.assertRequirementConstraints(type, requirement, request)
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
      active = request.endDate == null,
      pendingTransfer = false,
    )

    nsiManagerService.createNsiManager(nsi, request.manager, request.referralDate)
    validation.assertSupportedTypeLevel(nsi)

    if (features.nsiStatusHistory) {
      val statusHistory = NsiStatusHistory(
        nsi = nsi,
        status = status,
        date = nsi.statusDate,
        notes = nsi.notes,
      )
      nsi.statuses.add(statusHistory)
    }

    val entity = nsiRepository.saveAndFlush(nsi)
    audit.nsiId = entity.id

    nsiSystemContactService.createReferralContact(entity)
    nsiSystemContactService.createStatusContact(entity)
    nsiSystemContactService.createCommencedContact(entity)
    nsiSystemContactService.createTerminationContact(entity)

    return mapper.toDto(entity)
  }

  @PostAuthorize(
    "hasAuthority('${Authorities.PROVIDER}'.concat(returnObject.intendedProvider)) " +
      "and hasAuthority('${Authorities.PROVIDER}'.concat(returnObject.manager.provider))"
  )
  private fun getNsi(id: Long) = nsiRepository.findByIdOrNotFound(id)
}
