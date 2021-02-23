package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import uk.gov.justice.digital.hmpps.deliusapi.validation.AllowedValues
import uk.gov.justice.digital.hmpps.deliusapi.validation.Crn
import uk.gov.justice.digital.hmpps.deliusapi.validation.DependentFields
import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroups
import uk.gov.justice.digital.hmpps.deliusapi.validation.NotBlankWhenProvided
import uk.gov.justice.digital.hmpps.deliusapi.validation.OfficeLocationCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.ProviderCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StaffCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.TeamCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.TimeRanges
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@TimeRanges
@FieldGroups
data class NewContact(
  @field:Crn
  val offenderCrn: String,

  @field:NotBlank
  @field:Size(max = 10)
  @field:AllowedValues("\${contacts.allowed-types}")
  val type: String,

  @field:NotBlankWhenProvided
  @field:Size(max = 10)
  val outcome: String? = null,

  @field:ProviderCode
  val provider: String,

  @field:TeamCode
  val team: String,

  @field:StaffCode
  val staff: String,

  @field:OfficeLocationCode
  val officeLocation: String?,

  val date: LocalDate,

  @field:StartTime(name = "contact")
  val startTime: LocalTime?,

  @field:EndTime(name = "contact")
  @field:DependentFields("startTime")
  val endTime: LocalTime?,

  val alert: Boolean = false,

  val sensitive: Boolean = false,

  @field:Size(max = 4000)
  val notes: String?,

  @field:Size(max = 200)
  val description: String?,

  @field:Positive
  val eventId: Long? = null,

  @field:Positive
  @field:DependentFields("eventId")
  val requirementId: Long? = null,
)
