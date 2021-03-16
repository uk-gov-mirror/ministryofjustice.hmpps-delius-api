package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.updateNotes
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class SystemContactService(
  private val contactRepository: ContactRepository,
  private val offenderRepository: OffenderRepository,
  private val contactTypeRepository: ContactTypeRepository,
  private val providerRepository: ProviderRepository,
  private val nsiRepository: NsiRepository,
) {
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

    val entity = Contact(
      type = type,
      offender = offender,
      nsi = nsi,
      provider = provider,
      team = team,
      staff = staff,
      event = event,
      date = request.timestamp.toLocalDate(),
      startTime = request.timestamp.toLocalTime(),
    )

    entity.updateNotes(type.defaultHeadings, request.notes)

    contactRepository.saveAndFlush(entity)
  }

  fun createSystemEnforcementActionContact(contact: Contact) {
    val action = contact.enforcements.getOrNull(0)?.action ?: return

    val entity = Contact(
      type = action.contactType,
      offender = contact.offender,
      nsi = contact.nsi,
      requirement = contact.requirement,
      event = contact.event,
      provider = contact.provider,
      team = contact.team,
      staff = contact.staff,
      officeLocation = contact.officeLocation,
      date = LocalDate.now(),
      startTime = LocalTime.now(),
      sensitive = contact.sensitive,
    )

    entity.updateNotes("${contact.notes}\n${LocalDateTime.now()}\nEnforcement Action: ${action.description}")

    contactRepository.saveAndFlush(entity)
  }
}
