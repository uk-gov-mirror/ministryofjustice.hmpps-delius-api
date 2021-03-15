package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact

@Mapper
interface ContactMapper {
  fun toNew(src: ContactDto): NewContact

  @Mappings(
    Mapping(source = "offender.crn", target = "offenderCrn"),
    Mapping(source = "type.code", target = "type"),
    Mapping(source = "outcome.code", target = "outcome"),
    Mapping(target = "enforcement", expression = GET_ENFORCEMENT_CODE),
    Mapping(source = "provider.code", target = "provider"),
    Mapping(source = "team.code", target = "team"),
    Mapping(source = "staff.code", target = "staff"),
    Mapping(source = "officeLocation.code", target = "officeLocation"),
    Mapping(source = "event.id", target = "eventId"),
    Mapping(source = "requirement.id", target = "requirementId"),
    Mapping(source = "nsi.id", target = "nsiId"),
  )
  fun toDto(src: Contact): ContactDto

  @Mappings(
    Mapping(source = "outcome.code", target = "outcome"),
    Mapping(target = "enforcement", expression = GET_ENFORCEMENT_CODE),
    Mapping(source = "provider.code", target = "provider"),
    Mapping(source = "team.code", target = "team"),
    Mapping(source = "staff.code", target = "staff"),
    Mapping(source = "officeLocation.code", target = "officeLocation"),
    Mapping(target = "notes", ignore = true), // notes are immutable
  )
  fun toUpdate(src: Contact): UpdateContact

  companion object {
    private const val GET_ENFORCEMENT_CODE = "java(ContactMapper.Companion.getEnforcementCode(src))"
    val INSTANCE = Mappers.getMapper(ContactMapper::class.java)
    fun getEnforcementCode(contact: Contact) = contact.enforcements.getOrNull(0)?.action?.code
  }
}
