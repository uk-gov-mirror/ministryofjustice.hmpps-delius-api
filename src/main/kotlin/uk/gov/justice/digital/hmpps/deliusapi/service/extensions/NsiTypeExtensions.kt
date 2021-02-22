package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

private enum class NsiTypeLevel {
  OFFENDER,
  EVENT,
}

private typealias NsiTypeLevels = Set<NsiTypeLevel>

private fun NsiType.getSupportedLevels(): NsiTypeLevels {
  val levels = mutableSetOf<NsiTypeLevel>()
  if (this.offenderLevel) {
    levels.add(NsiTypeLevel.OFFENDER)
  }
  if (this.eventLevel) {
    levels.add(NsiTypeLevel.EVENT)
  }
  return levels.toSet()
}

private fun NewNsi.getLevel() = if (this.eventId == null) NsiTypeLevel.OFFENDER else NsiTypeLevel.EVENT

fun NsiType.assertSupportedLevel(newNsi: NewNsi) {
  val level = newNsi.getLevel()
  val supported = this.getSupportedLevels()
  if (supported.contains(level)) {
    return
  }

  val names = supported.joinToString(" or ") { it.name.toLowerCase() }
  throw BadRequestException(
    "NSI type with code ${this.code} supports association to $names only " +
      "& this request is attempting to associate to ${level.name.toLowerCase()}"
  )
}
