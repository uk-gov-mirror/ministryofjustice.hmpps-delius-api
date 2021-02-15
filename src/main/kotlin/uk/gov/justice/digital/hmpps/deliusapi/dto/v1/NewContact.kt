package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.TimeRange
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@TimeRange(message = "contact start and end times must form a valid range")
data class NewContact(
  @field:NotBlank
  @field:Pattern(regexp = "^[a-zA-Z][0-9]{6}\$", message = "must be a valid CRN")
  val offenderCrn: String,

  @field:NotBlank
  @field:Size(min = 1, max = 10)
  val contactType: String,

  @field:Size(min = 1, max = 10)
  val contactOutcome: String? = null,

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

  @field:NotNull
  val contactDate: LocalDate,

  @field:NotNull
  @field:StartTime
  val contactStartTime: LocalTime,

  @field:NotNull
  @field:EndTime
  val contactEndTime: LocalTime,

  val alert: Boolean = false,

  val sensitive: Boolean = false,

  @field:Size(min = 0, max = 4000)
  val notes: String?,

  @field:Size(min = 0, max = 200)
  val description: String?,

  @field:Positive
  val eventId: Long? = null,

  @field:Positive
  val requirementId: Long? = null,
) {
  @AssertTrue(message = "Cannot specify a requirement without an event")
  fun isEventProvidedWithRequirement(): Boolean {
    if (requirementId == null) {
      return true
    }
    return eventId != null
  }
}
