package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class NewContact(
  @field:NotBlank
  @field:Pattern(regexp = "^[a-zA-Z][0-9]{6}\$", message = "must be a valid CRN")
  val offenderCrn: String,

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
  val description: String?
)
