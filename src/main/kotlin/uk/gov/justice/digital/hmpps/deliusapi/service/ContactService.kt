package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.Contact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact

interface IContactService {
  fun createContact(request: NewContact): Contact
}

@Service
class ContactService : IContactService {
  override fun createContact(request: NewContact): Contact = Contact(
    id = 1,
    offenderId = request.offenderId,
    contactType = request.contactType,
    contactOutcome = request.contactOutcome,
    provider = request.provider,
    team = request.team,
    staff = request.staff,
    officeLocation = request.officeLocation,
    contactDate = request.contactDate,
    contactStartTime = request.contactStartTime,
    contactEndTime = request.contactEndTime,
    alert = request.alert,
    sensitiveContact = request.sensitiveContact,
    notes = request.notes,
    contactShortDescription = request.contactShortDescription
  )
}
