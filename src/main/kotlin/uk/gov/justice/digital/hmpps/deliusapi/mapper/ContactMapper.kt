package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team

@Mapper
interface ContactMapper {
  @Mapping(target = "id", ignore = true)
  fun toDto(src: NewContact): ContactDto

  @Mappings(
    Mapping(source = "offender.id", target = "offenderId"),
    Mapping(source = "contactType.code", target = "contactType"),
    Mapping(source = "contactOutcomeType.code", target = "contactOutcome"),
    Mapping(source = "provider.code", target = "provider"),
    Mapping(source = "team.code", target = "team"),
    Mapping(source = "staff.code", target = "staff"),
    Mapping(source = "officeLocation.code", target = "officeLocation"),
  )
  fun toDto(src: Contact): ContactDto

  fun toString(src: ContactType) = src.code

  fun toString(src: OfficeLocation) = src.code

  fun toString(src: Provider) = src.code

  fun toString(src: Staff) = src.code

  fun toString(src: Team) = src.code

  companion object {
    val INSTANCE = Mappers.getMapper(ContactMapper::class.java)
  }
}
