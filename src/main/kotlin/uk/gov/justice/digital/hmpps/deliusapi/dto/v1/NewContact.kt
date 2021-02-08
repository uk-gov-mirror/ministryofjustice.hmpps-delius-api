package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class NewContact(
  @field:Positive
  val offenderId: Long,

  @field:NotBlank
  @field:Size(min = 1, max = 10)
  val contactType: String,

  @field:NotBlank
  @field:Size(min = 1, max = 10)
  val contactOutcome: String,

  @field:NotBlank
  @field:Size(min = 3, max = 3, message = "size must be {min}")
  val provider: String,

  @field:NotBlank
  @field:Size(min = 6, max = 6, message = "size must be {min}")
  val team: String,

  @field:NotBlank
  @field:Size(min = 7, max = 7, message = "size must be {min}")
  val staff: String,

  @field:NotBlank
  @field:Size(min = 7, max = 7, message = "size must be {min}")
  val officeLocation: String,

  val contactDate: LocalDate,

  val contactStartTime: LocalTime,

  val contactEndTime: LocalTime,

  val alert: Boolean = false,

  val sensitive: Boolean = false,

  @field:Size(min = 0, max = 4096)
  val notes: String?,

  @field:Size(min = 0, max = 200)
  val contactShortDescription: String?
)
