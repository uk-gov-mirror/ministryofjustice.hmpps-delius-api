package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository

fun ContactRepository.getCurrentFailureToComply(event: Event): Long {
  // count all failure to comply contacts since the latter of either the date of sentencing or the last end of breach
  val sentenceDate = event.disposal?.date
  val lastBreachResetDate = listOfNotNull(sentenceDate, event.breachEnd).maxOrNull()
  return countFailureToComply(event.id, lastBreachResetDate)
}
