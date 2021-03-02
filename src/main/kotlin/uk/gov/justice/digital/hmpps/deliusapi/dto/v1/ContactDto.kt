package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.LocalTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContactDto(
  val id: Long,
  val offenderCrn: String,
  val nsiId: Long? = null,
  val type: String,
  val outcome: String?,
  val provider: String,
  val team: String,
  val staff: String,
  val officeLocation: String?,
  val date: LocalDate,
  val startTime: LocalTime?,
  val endTime: LocalTime?,
  val alert: Boolean,
  val sensitive: Boolean,
  val notes: String? = null,
  val description: String? = null,
  val eventId: Long? = null,
  val requirementId: Long? = null,
)
