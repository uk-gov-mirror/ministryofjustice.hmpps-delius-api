package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.config.Authorities
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.B
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.Y
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCrnOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getEventOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getRequirementOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStaffOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getTeamOrBadRequest
import java.lang.IllegalArgumentException
import java.time.LocalDate

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val providerRepository: ProviderRepository,
  private val nsiRepository: NsiRepository,
) {

  @PreAuthorize("hasAuthority('${Authorities.PROVIDER}'.concat(#request.provider))")
  @Auditable(AuditableInteraction.ADD_CONTACT)
  fun createContact(request: NewContact): ContactDto {
    val offender = offenderRepository.findByCrnOrBadRequest(request.offenderCrn)

    val audit = AuditContext.get(AuditableInteraction.ADD_CONTACT)
    audit.offenderId = offender.id

    val type = contactTypeRepository.findSelectableByCode(request.type)
      ?: throw BadRequestException("Contact type with code '${request.type}' does not exist")

    if (type.outcomeFlag == Y && request.outcome == null && request.date.isBefore(LocalDate.now())) {
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

    val event = if (request.eventId == null) null else offender.getEventOrBadRequest(request.eventId)
    val requirement = if (event == null || request.requirementId == null) null
    else offender.getRequirementOrBadRequest(event, request.requirementId)

    val provider = providerRepository.findByCodeAndSelectableIsTrue(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val team = provider.getTeamOrBadRequest(request.team)
    val staff = team.getStaffOrBadRequest(request.staff)

    if (type.locationFlag == Y && request.officeLocation == null) {
      throw BadRequestException("Location is required for contact type '${request.type}'")
    }

    val officeLocation =
      when {
        request.officeLocation != null && (type.locationFlag == Y || type.locationFlag == B) ->
          team.officeLocations?.find { it.code == request.officeLocation }
        else -> null
      }

    if (type.locationFlag == Y && officeLocation == null) {
      throw BadRequestException("Team with code '${request.team}' does not exist at office location '${request.officeLocation}'")
    }

    if (type.attendanceContact && request.startTime == null) {
      throw BadRequestException("Contact type '${type.code}' requires a start time")
    }

    if (type.recordedHoursCredited && outcome != null && request.endTime == null) {
      throw BadRequestException("Contact type '${type.code}' requires an end time when an outcome is provided")
    }

    val nsi = if (request.nsiId == null) null
    else nsiRepository.findByIdOrNull(request.nsiId)
      ?: throw IllegalArgumentException("NSI with id '${request.nsiId}' does not exist")

    val contact = Contact(
      offender = offender,
      nsi = nsi,
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

  /**
   * Adds a system generated contact record.
   * System generated contacts are non-audited & non-validated (other than referential integrity of course).
   */
  fun createSystemContact(request: NewSystemContact) {
    val offender = offenderRepository.findByIdOrNull(request.offenderId)
      ?: throw IllegalArgumentException("Offender with id '${request.offenderId}' does not exist")
    val provider = providerRepository.findByIdOrNull(request.providerId)
      ?: throw IllegalArgumentException("Provider with id '${request.providerId}' does not exist")
    val team = provider.teams?.find { it.id == request.teamId }
      ?: throw IllegalArgumentException("Team with id '${request.teamId}' does not exist on provider '${provider.code}'")
    val staff = team.staff?.find { it.id == request.staffId }
      ?: throw IllegalArgumentException("Staff with id '${request.staffId}' does not exist on team '${team.code}'")

    val event = if (request.eventId == null) null
    else offender.events?.find { it.id == request.eventId }
      ?: throw IllegalArgumentException("Event with id '${request.eventId}' does not exist on offender '${offender.id}'")

    val nsi = if (request.nsiId == null) null
    else nsiRepository.findByIdOrNull(request.nsiId)
      ?: throw IllegalArgumentException("NSI with id '${request.nsiId}' does not exist")

    val type = when {
      request.typeId != null ->
        contactTypeRepository.findByIdOrNull(request.typeId)
          ?: throw IllegalArgumentException("Contact type with id '${request.typeId}' does not exist")
      request.type != null ->
        contactTypeRepository.findByCode(request.type.code)
          ?: throw IllegalArgumentException("Contact type with code '${request.type}' does not exist")
      else -> throw IllegalArgumentException("Must provider type id or code")
    }

    val contact = Contact(
      type = type,
      offender = offender,
      nsi = nsi,
      provider = provider,
      team = team,
      staff = staff,
      event = event,
      date = request.timestamp.toLocalDate(),
      startTime = request.timestamp.toLocalTime(),
      notes = request.notes,

      // TODO need to set to something from configuration
      partitionAreaId = 0,
      staffEmployeeId = 1, // <- not actually sure what this is it should reference a PROVIDER_EMPLOYEE
      teamProviderId = 1,
    )

    contactRepository.saveAndFlush(contact)
  }
}
