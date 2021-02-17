package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import java.time.LocalDate

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val providerRepository: ProviderRepository,
) {

  @Auditable(AuditableInteraction.ADD_CONTACT)
  fun createContact(request: NewContact): ContactDto {
    val offender = offenderRepository.findByCrn(request.offenderCrn)
      ?: throw BadRequestException("Offender with code '${request.offenderCrn}' does not exist")

    val audit = AuditContext.get(AuditableInteraction.ADD_CONTACT)
    audit.offenderId = offender.id

    val type = contactTypeRepository.findByCode(request.type)
      ?: throw BadRequestException("Contact type with code '${request.type}' does not exist")

    if (type.outcomeFlag && request.outcome == null && request.date.isBefore(LocalDate.now())) {
      throw BadRequestException("Contact type '${type.code}' requires an outcome type")
    }

    if (request.alert && !type.alertFlag) {
      throw BadRequestException("Contact type '${type.code}' does not support alert")
    }

    val outcome = if (request.outcome != null)
      type.outcomeTypes?.find { it.code == request.outcome }
        ?: throw BadRequestException("Contact type with code '${request.type}' does not support outcome code '${request.outcome}'")
    else null

    if (outcome != null && request.date.isAfter(LocalDate.now()) && !(outcome.compliantAcceptable == true && outcome.attendance == false)) {
      throw BadRequestException("Outcome code '${request.outcome}' not a permissible absence - only permissible absences can be recorded for a future attendance")
    }

    val event = if (request.eventId != null)
      offender.events?.find { it.id == request.eventId }
        ?: throw BadRequestException("Event with id '${request.eventId}' does not exist on offender '${offender.crn}'")
    else null

    val requirement = if (request.requirementId != null) {
      if (event == null) {
        throw BadRequestException("Cannot specify a requirement without an event")
      }
      event.disposals?.flatMap { it.requirements ?: listOf() }?.find { it.id == request.requirementId && it.offenderId == offender.id }
        ?: throw BadRequestException("Requirement with id '${request.requirementId}' does not exist on event '${request.eventId}' and offender '${offender.crn}'")
    } else null

    val provider = providerRepository.findByCode(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val officeLocation = provider.officeLocations?.find { it.code == request.officeLocation }
      ?: throw BadRequestException("Office location with code '${request.officeLocation}' does not exist in provider '${request.provider}'")

    val team = officeLocation.teams?.find { it.code == request.team }
      ?: throw BadRequestException("Team with code '${request.team}' does not exist at office location '${request.officeLocation}'")

    val staff = team.staff?.find { it.code == request.staff }
      ?: throw BadRequestException("Staff with officer code '${request.staff}' does not exist in team '${request.team}'")

    val contact = Contact(
      offender = offender,
      type = type,
      outcome = outcome,
      provider = provider,
      team = team,
      staff = staff,
      event = event,
      requirement = requirement,
      officeLocation = officeLocation,
      date = request.date,
      startTime = request.startTime,
      endTime = request.endTime,
      alert = request.alert,
      sensitive = request.sensitive,
      notes = request.notes,
      description = request.description,

      // TODO need to set to something from configuration
      partitionAreaId = 0,
      staffEmployeeId = 1, // <- not actually sure what this is it should reference a PROVIDER_EMPLOYEE
      teamProviderId = 1,
    )

    val entity = contactRepository.saveAndFlush(contact)
    return ContactMapper.INSTANCE.toDto(entity)
  }
}
