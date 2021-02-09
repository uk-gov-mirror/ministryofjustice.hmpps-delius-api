package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository

@Service
class ContactService(
  val contactRepository: ContactRepository,
  val offenderRepository: OffenderRepository
) {

  fun createContact(request: NewContact): ContactDto {
    val offender = offenderRepository.findByCrn(request.offenderCrn).orElse(null)
      ?: throw BadRequestException("Offender with crn ${request.offenderCrn} does not exist")

    val contact = Contact(
      offender = offender,

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
      description = request.description,

      // TODO need to set to something from configuration
      partitionAreaId = 0,
      staffEmployeeId = 1,
      teamProviderId = 1,
    )

    val entity = contactRepository.saveAndFlush(contact)

    return ContactMapper.INSTANCE.toDto(entity)
  }
}
