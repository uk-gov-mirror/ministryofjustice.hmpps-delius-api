package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

fun Offender.getEventOrBadRequest(eventId: Long): Event {
  val event = events?.find { it.id == eventId }
    ?: throw BadRequestException("Event with id '$eventId' does not exist on offender '$crn'")

  if (!event.active) {
    throw BadRequestException("Event with id '$eventId' is not active")
  }

  return event
}

fun Offender.getRequirementOrBadRequest(event: Event, requirementId: Long): Requirement {
  val requirement = event.disposals
    ?.flatMap { it.requirements ?: listOf() }
    ?.find { it.id == requirementId && it.offenderId == id }
    ?: throw BadRequestException("Requirement with id '$requirementId' does not exist on event '$id' and offender '$crn'")

  if (!requirement.active) {
    throw BadRequestException("Requirement with id '$requirementId' is not active")
  }

  return requirement
}
