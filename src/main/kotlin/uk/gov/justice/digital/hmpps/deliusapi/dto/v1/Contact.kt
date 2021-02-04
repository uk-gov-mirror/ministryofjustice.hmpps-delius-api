package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import java.time.LocalDate
import java.time.LocalTime

data class Contact(
  val id: Int,
  val offenderId: Int,
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
  val sensitiveContact: Boolean,
  val notes: String?,
  val contactShortDescription: String?
)
