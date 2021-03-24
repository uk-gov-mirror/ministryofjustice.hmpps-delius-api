package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import java.time.LocalDate

/**
 * Determines whether the event is in breach on the specified date
 */
fun Event.isInBreachOn(date: LocalDate) = inBreach || (breachEnd != null && breachEnd!! > date)
