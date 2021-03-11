package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.deliusapi.validation.Crn
import uk.gov.justice.digital.hmpps.deliusapi.validation.DependentFields
import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroups
import uk.gov.justice.digital.hmpps.deliusapi.validation.NotBlankWhenProvided
import uk.gov.justice.digital.hmpps.deliusapi.validation.NsiTypeCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.ProviderCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTimes
import uk.gov.justice.digital.hmpps.deliusapi.validation.TimeRanges
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

/**
 * A request to create a new non-statutory intervention.
 */
@TimeRanges
@FieldGroups
data class NewNsi(
  @ApiModelProperty("The type of the NSI")
  @NsiTypeCode
  val type: String,

  @ApiModelProperty("The sub type of the NSI, this is required for some NSI types")
  @NsiTypeCode
  val subType: String? = null,

  @ApiModelProperty("The offender CRN")
  @Crn
  val offenderCrn: String,

  @ApiModelProperty("An optional event ID that the new NSI will be associated to")
  @field:Positive
  val eventId: Long? = null,

  @ApiModelProperty("An optional requirement ID that the new NSI will be associated to, an event is required for association to a requirement")
  @DependentFields("eventId")
  @field:Positive
  val requirementId: Long? = null,

  @ApiModelProperty("The date of the referral")
  @field:PastOrPresent
  @StartTimes(
    StartTime(name = "referral-to-expected-start"),
    StartTime(name = "referral-to-start"),
    StartTime(name = "referral-to-status"),
  )
  val referralDate: LocalDate,

  @ApiModelProperty("The expected intervention start date")
  @EndTime(name = "referral-to-expected-start")
  @StartTime(name = "expected-date-range")
  val expectedStartDate: LocalDate?,

  @ApiModelProperty("The expected intervention end date")
  @EndTime(name = "expected-date-range")
  @DependentFields("expectedStartDate")
  val expectedEndDate: LocalDate?,

  @ApiModelProperty("The actual intervention start date")
  @field:PastOrPresent
  @EndTime(name = "referral-to-start")
  @StartTime(name = "date-range")
  val startDate: LocalDate?,

  @ApiModelProperty("The actual intervention end date, an end date is required if outcome is provided")
  @field:PastOrPresent
  @EndTime(name = "date-range")
  @DependentFields("outcome", "startDate")
  val endDate: LocalDate?,

  @ApiModelProperty("The length of the intervention, the units of this are determined by the NSI type selected")
  @field:Positive
  val length: Long?,

  @ApiModelProperty("The status of the intervention")
  @field:NotBlank
  @field:Size(max = 20)
  val status: String,

  @ApiModelProperty("The status date")
  @field:PastOrPresent
  @EndTime(name = "referral-to-status")
  val statusDate: LocalDateTime,

  @ApiModelProperty("The outcome of the intervention. required if an end date is provided")
  @NotBlankWhenProvided
  @field:Size(max = 100)
  @DependentFields("endDate")
  val outcome: String?,

  @ApiModelProperty("General notes")
  @field:Size(max = 4000)
  val notes: String?,

  @ApiModelProperty("The provider")
  @ProviderCode
  val intendedProvider: String,

  @ApiModelProperty("The active manager associated to this NSI")
  val manager: NewNsiManager,
)
