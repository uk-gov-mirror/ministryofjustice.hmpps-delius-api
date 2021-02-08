package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import java.time.LocalDateTime

@Service
class ContactService(val repository: ContactRepository) {
  fun createContact(request: NewContact): ContactDto {
    val contact = Contact(
      offender = Offender(id = request.offenderId),

      // TODO set all these up from request codes.
      // TODO hibernate should be able to populate the codes for us here
      contactType = ContactType(id = 1, code = request.contactType),
      contactOutcomeType = ContactOutcomeType(id = 1, code = request.contactOutcome),
      provider = Provider(id = 1, code = request.provider),
      team = Team(id = 1, code = request.team),
      staff = Staff(id = 1, code = request.staff),
      officeLocation = OfficeLocation(id = 1, code = request.officeLocation),

      contactDate = request.contactDate,
      contactStartTime = request.contactStartTime,
      contactEndTime = request.contactEndTime,
      alert = request.alert,
      sensitive = request.sensitive,
      notes = request.notes,

      // TODO what is this field?
      // contactShortDescription = request.contactShortDescription

      // TODO need to set to something from configuration
      createdByUserId = 1,
      lastUpdatedUserId = 1,
      partitionAreaId = 0,
      staffEmployeeId = 1,
      teamProviderId = 1,

      createdDateTime = LocalDateTime.now(),
      lastUpdatedDateTime = LocalDateTime.now(),
    )

    val entity = repository.saveAndFlush(contact)

    return ContactMapper.INSTANCE.toDto(entity)
  }
}
