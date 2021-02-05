package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team

@Mapper
interface ContactMapper {
  fun toDto(entity: Contact): ContactDto

  fun toString(src: ContactOutcomeType) = src.code

  fun toString(src: ContactType) = src.code

  fun toString(src: OfficeLocation) = src.code

  fun toString(src: Provider) = src.code

  fun toString(src: Staff) = src.code

  fun toString(src: Team) = src.code

  companion object {
    val INSTANCE = Mappers.getMapper(ContactMapper::class.java)
  }
}
