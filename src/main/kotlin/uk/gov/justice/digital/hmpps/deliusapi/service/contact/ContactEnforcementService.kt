package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EventRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.isEnforcementUnderReview
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getBreachType
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.isInBreachOn

@Service
class ContactEnforcementService(
  private val contactRepository: ContactRepository,
  private val eventRepository: EventRepository,
  private val systemContactService: SystemContactService,
  private val contactBreachService: ContactBreachService,
) {
  fun updateFailureToComply(contact: Contact) {
    // 1. Is the contact type National Standards & has an event ID?
    val event = contact.event ?: return

    val currentFtc: Long by lazy { contactRepository.getCurrentFailureToComply(event) }

    if (!contact.type.nationalStandardsContact) {
      // Is the contact type Release From Custody (EREL) -> Check & Update Event FTC
      if (contact.type.code == WellKnownContactType.RELEASE_FROM_CUSTODY.code) {
        event.ftcCount = currentFtc
        eventRepository.saveAndFlush(event)
      }
      return
    }

    // Determine whether the contact starts or ends a breach
    val enforcementBreachType = if (contact.outcome?.actionRequired == true)
      contact.enforcement?.action?.contactType?.getBreachType() else null

    // 2. Check & Update Event FTC
    if (enforcementBreachType != BreachType.END && currentFtc != event.ftcCount) {
      event.ftcCount = currentFtc
      eventRepository.saveAndFlush(event)
    }

    // 3. Does the contact have an outcome?
    val outcome = contact.outcome ?: return

    // 4. Is the outcome Compliant Acceptable?
    if (outcome.compliantAcceptable != false) return

    // 5. Enforcement should not initiate breach
    if (enforcementBreachType == BreachType.START) return

    // 7. Does the event have a breach end date of null OR is it dated on or before the contact date
    if (event.isInBreachOn(contact.date)) return

    // 8. Is the event sentenced?
    if (event.disposal?.type?.sentenceType == null) return

    // 9. Has the Breach limit been breached
    val limit = event.disposal?.type?.failureToComplyLimit
    if (limit == null || event.ftcCount < limit) {
      // will trigger a breach once FTC limit is reached
      return
    }

    // 6. Is Enforcement Under Review & is event in breach & breach end date is on or before the contact date
    // Note: second part to this is already covered above in 7.
    //       The order differs from Delius & flow to short circuit the IO required for this check.
    val isUnderReview = contactRepository.isEnforcementUnderReview(
      event.id,
      WellKnownContactType.REVIEW_ENFORCEMENT_STATUS.code, event.breachEnd
    )
    if (isUnderReview) {
      return
    }

    // this failure to comply will trigger a breach, create a review contact & update breach on event
    val reviewContact = systemContactService.createLinkedSystemContact(contact, WellKnownContactType.REVIEW_ENFORCEMENT_STATUS)
    contactBreachService.updateBreachOnInsertContact(reviewContact)
  }
}
