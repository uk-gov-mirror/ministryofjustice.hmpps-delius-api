package uk.gov.justice.digital.hmpps.deliusapi.repository.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Wraps a date & optional time, will normalise to a LocalDateTime.
 */
class LocalDateTimeWrapper(val date: LocalDate, val time: LocalTime?) {
  val dateTime: LocalDateTime =
    if (time == null) LocalDateTime.of(date, LocalTime.MIDNIGHT)
    else LocalDateTime.of(date, time)
}
