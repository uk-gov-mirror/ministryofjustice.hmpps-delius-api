package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import java.time.LocalDate
import java.time.LocalTime

data class ContactDto(
  val id: Long,
  val offenderCrn: String,
  val contactType: String,
  val contactOutcome: String,
  val provider: String,
  val team: String,
  val staff: String,
  val officeLocation: String,
  val contactDate: LocalDate,
  val contactStartTime: LocalTime,
  val contactEndTime: LocalTime,
  val alert: Boolean,
  val sensitive: Boolean,
  val notes: String?,
  val description: String?
)
