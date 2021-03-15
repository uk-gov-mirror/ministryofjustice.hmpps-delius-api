package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import java.time.Duration

fun Contact.getDuration(): Duration {
  if (startTime != null && endTime != null) {
    return Duration.between(startTime, endTime)
  }
  return Duration.ZERO
}
