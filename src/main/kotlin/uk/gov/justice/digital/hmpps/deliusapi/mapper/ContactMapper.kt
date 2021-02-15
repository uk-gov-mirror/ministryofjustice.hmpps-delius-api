package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact

@Mapper
interface ContactMapper {
  fun toNew(src: ContactDto): NewContact

  @Mappings(
    Mapping(source = "offender.crn", target = "offenderCrn"),
    Mapping(source = "contactType.code", target = "contactType"),
    Mapping(source = "contactOutcomeType.code", target = "contactOutcome"),
    Mapping(source = "provider.code", target = "provider"),
    Mapping(source = "team.code", target = "team"),
    Mapping(source = "staff.code", target = "staff"),
    Mapping(source = "officeLocation.code", target = "officeLocation"),
    Mapping(source = "event.id", target = "eventId"),
    Mapping(source = "requirement.id", target = "requirementId"),
  )
  fun toDto(src: Contact): ContactDto

  companion object {
    val INSTANCE = Mappers.getMapper(ContactMapper::class.java)
  }
}
