package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.config.Authorities
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.B
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.N
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
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.isPermissibleAbsence
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

    if (outcome != null && request.date.isAfter(LocalDate.now()) && !outcome.isPermissibleAbsence()) {
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

    if (type.locationFlag == N && request.officeLocation != null) {
      throw BadRequestException("Contact type '${request.type}' does not support a location")
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

    if (type.recordedHoursCredited && request.endTime == null) {
      throw BadRequestException("Contact type '${type.code}' requires an end time")
    }

    val nsi = if (request.nsiId == null) null
    else nsiRepository.findByIdOrNull(request.nsiId)
      ?: throw IllegalArgumentException("NSI with id '${request.nsiId}' does not exist")

    if (requirement != null) {
      // Contact is at "Whole Order" level - type must be whole order level OR have matching requirement type category.
      if (!type.wholeOrderLevel && type.requirementTypeCategories?.any { it.id == requirement.typeCategory?.id } != true) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for an requirement in category '${requirement.typeCategory?.code}'")
      }
    } else if (event != null) {
      // Contact is at event level - type must support relevant pre/post CJA 2003 status of event.
      // It looks like in Delius that these are not mutually exclusive!
      val isLegacy = event.disposals?.any { it.type?.legacyOrder == true } ?: false
      if (isLegacy && !type.legacyOrderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a pre CJA 2003 event")
      }
      val isCja = event.disposals?.any { it.type?.cja2003Order == true } ?: false
      if (isCja && !type.cjaOrderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a CJA 2003 event")
      }
    } else if (nsi != null) {
      // Contact is at nsi level - contact type must support nsi type
      if (type.nsiTypes?.any { it.id == nsi.type?.id } != true) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for an NSI with type '${nsi.type?.code}'")
      }
    } else {
      // Contact is at offender level
      if (!type.offenderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate at offender level")
      }
    }

    // If contact is an attendance contact & has a start & end time then check for appointment clashes
    assertFutureAppointmentClashes(type, offender, request)

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
          ?: throw IllegalArgumentException("Contact type with code '${request.type.code}' does not exist")
      else -> throw IllegalArgumentException("Must provide type id or code")
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

  private fun assertFutureAppointmentClashes(type: ContactType, offender: Offender, request: NewContact) {
    if (!type.attendanceContact || request.endTime == null || !request.date.isAfter(LocalDate.now())) {
      return
    }

    val clashes = contactRepository.findClashingAttendanceContacts(
      offender.id,
      request.date,
      request.startTime,
      request.endTime,
    )

    if (clashes.isNotEmpty()) {
      val ids = clashes.joinToString(", ") { "'${it.id}'" }
      throw BadRequestException(
        "Contact type '${type.code}' is an attendance type so must not clash with any other " +
          "attendance contacts but clashes with contacts with ids $ids"
      )
    }
  }
}
