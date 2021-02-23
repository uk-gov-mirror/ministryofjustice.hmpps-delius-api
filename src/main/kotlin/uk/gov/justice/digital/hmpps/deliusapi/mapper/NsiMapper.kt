package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.factory.Mappers
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager

@Mapper
interface NsiMapper {
  @Mappings(
    Mapping(target = "manager", expression = "java(toNew(src.getManagers().get(0)))"),
  )
  fun toNew(src: NsiDto): NewNsi

  fun toNew(src: NsiManagerDto): NewNsiManager

  @Mappings(
    Mapping(source = "type.code", target = "type"),
    Mapping(source = "subType.code", target = "subType"),
    Mapping(source = "offender.crn", target = "offenderCrn"),
    Mapping(source = "event.id", target = "eventId"),
    Mapping(source = "requirement.id", target = "requirementId"),
    Mapping(source = "status.code", target = "status"),
    Mapping(source = "outcome.code", target = "outcome"),
    Mapping(source = "intendedProvider.code", target = "intendedProvider"),
  )
  fun toDto(src: Nsi): NsiDto

  @Mappings(
    Mapping(source = "staff.code", target = "staff"),
    Mapping(source = "team.code", target = "team"),
    Mapping(source = "provider.code", target = "provider"),
  )
  fun toDto(src: NsiManager): NsiManagerDto

  companion object {
    val INSTANCE = Mappers.getMapper(NsiMapper::class.java)
  }
}
