package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactOutcomeTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditService
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val contactOutcomeTypeRepository: ContactOutcomeTypeRepository,
  private val providerRepository: ProviderRepository,
  private val auditService: AuditService
) {

  fun createContact(request: NewContact): ContactDto {
    val type = contactTypeRepository.findByCode(request.contactType)
      ?: throw BadRequestException("Contact type with code '${request.contactType}' does not exist")

    if (request.alert && !type.contactAlertFlag) {
      throw BadRequestException("Contact type '${type.code}' does not support alert")
    }

    val offender = offenderRepository.findByCrn(request.offenderCrn)
      ?: throw BadRequestException("Offender with code '${request.offenderCrn}' does not exist")

    val provider = providerRepository.findByCode(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val officeLocation = provider.officeLocations?.find { it.code == request.officeLocation }
      ?: throw BadRequestException("Office location with code '${request.officeLocation}' does not exist in provider '${request.provider}'")

    val team = officeLocation.teams?.find { it.code == request.team }
      ?: throw BadRequestException("Team with code '${request.team}' does not exist at office location '${request.officeLocation}'")

    val staff = team.staff?.find { it.code == request.staff }
      ?: throw BadRequestException("Staff with officer code '${request.staff}' does not exist in team '${request.team}'")

    val outcome = contactOutcomeTypeRepository.findByCode(request.contactOutcome)
      ?: throw BadRequestException("Contact outcome with code '${request.contactOutcome}' does not exist")

    val staffEmployeeId = 1L

    val contact = Contact(
      offender = offender,
      contactType = type,
      contactOutcomeType = outcome,
      provider = provider,
      team = team,
      staff = staff,
      officeLocation = officeLocation,
      contactDate = request.contactDate,
      contactStartTime = request.contactStartTime,
      contactEndTime = request.contactEndTime,
      alert = request.alert,
      sensitive = request.sensitive,
      notes = request.notes,
      description = request.description,

      // TODO need to set to something from configuration
      partitionAreaId = 0,
      staffEmployeeId = staffEmployeeId,
      teamProviderId = 1,
    )

    return try {
      val entity = contactRepository.saveAndFlush(contact)
      auditService.successfulInteraction(staffEmployeeId, offender.id, AuditableInteraction.ADD_CONTACT)
      ContactMapper.INSTANCE.toDto(entity)
    } catch (e: RuntimeException) {
      auditService.failedInteraction(staffEmployeeId, offender.id, AuditableInteraction.ADD_CONTACT)
      throw e
    }
  }
}
