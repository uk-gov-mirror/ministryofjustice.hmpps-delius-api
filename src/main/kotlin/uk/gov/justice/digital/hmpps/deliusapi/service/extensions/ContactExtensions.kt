package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration

fun Contact.getDuration(): Duration {
  if (startTime != null && endTime != null) {
    return Duration.between(startTime, endTime)
  }
  return Duration.ZERO
}

fun Contact.updateOutcome(value: ContactOutcomeType?) {
  outcome = value
  attended = value?.attendance
  complied = value?.compliantAcceptable

  // If offender has complied and attended (outcome is acceptable) then set any hours credited
  if (type.recordedHoursCredited && attended == true && complied == true) {
    hoursCredited = BigDecimal.valueOf(getDuration().toMinutes())
      .divide(BigDecimal(60))
      .setScale(2, RoundingMode.HALF_UP)
      .toDouble()
  }
}
