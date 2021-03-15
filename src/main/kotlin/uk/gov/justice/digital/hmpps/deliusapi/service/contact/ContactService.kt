package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.CreateOrUpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCrnOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.security.ProviderRequestAuthority
import uk.gov.justice.digital.hmpps.deliusapi.security.ProviderResponseAuthority
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getEventOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getRequirementOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStaffOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getTeamOrBadRequest
import java.lang.IllegalArgumentException

@Service
class ContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val providerRepository: ProviderRepository,
  private val nsiRepository: NsiRepository,
  private val mapper: ContactMapper,
  private val validation: ContactValidationService,
) {

  companion object {
    // TODO is separator correct?
    const val NOTES_SEPARATOR = "\n\n---------\n\n"
  }

  @ProviderResponseAuthority
  fun getUpdateContact(id: Long): UpdateContact {
    val contact = contactRepository.findByIdOrNull(id)
      ?: throw NotFoundException.byId<Contact>(id)

    return mapper.toUpdate(contact)
  }

  @ProviderRequestAuthority
  @Auditable(AuditableInteraction.UPDATE_CONTACT)
  fun updateContact(id: Long, request: UpdateContact): ContactDto {
    val entity = contactRepository.findByIdOrNull(id)
      ?: throw NotFoundException.byId<Contact>(id)

    if (entity.type.editable != true) {
      throw BadRequestException("Contact type '${entity.type.code}' is not editable")
    }

    val audit = AuditContext.get(AuditableInteraction.UPDATE_CONTACT)
    audit.contactId = entity.id

    validation.validateContactType(request, entity.type)
    val (provider, team, staff) = getProviderTeamStaff(request)

    // If contact is an attendance contact & has a start & end time then check for appointment clashes
    validation.validateFutureAppointmentClashes(request, entity.type, entity.offender, entity.id)

    entity.outcome = validation.validateOutcomeType(request, entity.type)
    validation.setOutcomeMeta(entity)

    entity.officeLocation = validation.validateOfficeLocation(request, entity.type, team)

    entity.provider = provider
    entity.team = team
    entity.staff = staff
    entity.date = request.date
    entity.startTime = request.startTime
    entity.endTime = request.endTime
    entity.alert = request.alert
    entity.sensitive = request.sensitive
    entity.description = request.description
    entity.notes = getNotes(entity.notes, request.notes)

    if (entity.enforcements.size > 1) {
      throw RuntimeException("Cannot determine which enforcement to use on contact with id '${entity.id}'")
    }

    if (request.enforcement != entity.enforcements.getOrNull(0)?.action?.code) {
      val enforcement = validation.validateEnforcement(request, entity.type, entity.outcome)
      entity.enforcements.clear()
      if (enforcement != null) {
        enforcement.contact = entity
        entity.enforcements.add(enforcement)
      }
    }

    contactRepository.saveAndFlush(entity)
    return mapper.toDto(entity)
  }

  @ProviderRequestAuthority
  @Auditable(AuditableInteraction.ADD_CONTACT)
  fun createContact(request: NewContact): ContactDto {
    val offender = offenderRepository.findByCrnOrBadRequest(request.offenderCrn)

    val audit = AuditContext.get(AuditableInteraction.ADD_CONTACT)
    audit.offenderId = offender.id

    // General validation
    val type = contactTypeRepository.findSelectableByCode(request.type)
      ?: throw BadRequestException("Contact type with code '${request.type}' does not exist")

    validation.validateContactType(request, type)
    val outcome = validation.validateOutcomeType(request, type)
    val enforcement = validation.validateEnforcement(request, type, outcome)

    val (provider, team, staff) = getProviderTeamStaff(request)
    val officeLocation = validation.validateOfficeLocation(request, type, team)

    // If contact is an attendance contact & has a start & end time then check for appointment clashes
    validation.validateFutureAppointmentClashes(request, type, offender)

    // Associated entity validation
    val event = if (request.eventId == null) null else offender.getEventOrBadRequest(request.eventId)
    val requirement = if (event == null || request.requirementId == null) null
    else offender.getRequirementOrBadRequest(event, request.requirementId)

    val nsi = if (request.nsiId == null) null
    else nsiRepository.findByIdOrNull(request.nsiId)
      ?: throw BadRequestException("NSI with id '${request.nsiId}' does not exist")

    validation.validateAssociatedEntity(type, requirement, event, nsi)

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
      notes = getNotes(type.defaultHeadings, request.notes),
      description = request.description,

      // TODO need to set to something from configuration
      partitionAreaId = 0,
      staffEmployeeId = 1, // <- not actually sure what this is it should reference a PROVIDER_EMPLOYEE
      teamProviderId = 1,
    )

    validation.setOutcomeMeta(contact)

    if (enforcement != null) {
      enforcement.contact = contact
      contact.enforcements.add(enforcement)
    }

    val entity = contactRepository.saveAndFlush(contact)
    return mapper.toDto(entity)
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
      notes = getNotes(type.defaultHeadings, request.notes),

      // TODO need to set to something from configuration
      partitionAreaId = 0,
      staffEmployeeId = 1, // <- not actually sure what this is it should reference a PROVIDER_EMPLOYEE
      teamProviderId = 1,
    )

    contactRepository.saveAndFlush(contact)
  }

  private fun getProviderTeamStaff(request: CreateOrUpdateContact): Triple<Provider, Team, Staff> {
    val provider = providerRepository.findByCodeAndSelectableIsTrue(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val team = provider.getTeamOrBadRequest(request.team)
    val staff = team.getStaffOrBadRequest(request.staff)
    return Triple(provider, team, staff)
  }

  private fun getNotes(vararg sections: String?) = sections
    .filterNotNull()
    .joinToString(NOTES_SEPARATOR)
}
