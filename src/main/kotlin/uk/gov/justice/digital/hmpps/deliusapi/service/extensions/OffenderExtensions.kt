package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

fun Offender.getEvent(eventId: Long?): Event? {
  val event = if (eventId == null) null
  else this.events?.find { it.id == eventId }
    ?: throw BadRequestException("Event with id '$eventId' does not exist on offender '${this.crn}'")

  if (event?.active == false) {
    throw BadRequestException("Event with id '$eventId' is not active")
  }

  return event
}

fun Offender.getRequirement(event: Event?, requirementId: Long?): Requirement? {
  if (event == null) {
    // we're not checking if the requirement id is specified here as it should already be handled by field validation.
    return null
  }

  val requirement = if (requirementId == null) null else
    event.disposals?.flatMap { it.requirements ?: listOf() }?.find { it.id == requirementId && it.offenderId == this.id }
      ?: throw BadRequestException("Requirement with id '$requirementId' does not exist on event '${this.id}' and offender '${this.crn}'")

  if (requirement?.active == false) {
    throw BadRequestException("Requirement with id '$requirementId' is not active")
  }

  return requirement
}
