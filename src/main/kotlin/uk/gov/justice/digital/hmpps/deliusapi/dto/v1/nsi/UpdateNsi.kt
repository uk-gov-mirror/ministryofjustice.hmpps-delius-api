package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.deliusapi.validation.EndTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroup
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroupType
import uk.gov.justice.digital.hmpps.deliusapi.validation.FieldGroups
import uk.gov.justice.digital.hmpps.deliusapi.validation.NotBlankWhenProvided
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTime
import uk.gov.justice.digital.hmpps.deliusapi.validation.StartTimes
import uk.gov.justice.digital.hmpps.deliusapi.validation.TimeRanges
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@TimeRanges
@FieldGroups
data class UpdateNsi(
  /**
   * Note the referral date is actually immutable, this is here in order to run any validation rules that require it.
   * We can get away with this currently as we aren't exposing a PUT endpoint
   * i.e. this will be populated from the existing NSI.
   */
  @ApiModelProperty("The date of the referral")
  @field:PastOrPresent
  @StartTimes(
    StartTime(name = "referral-to-expected-start"),
    StartTime(name = "referral-to-start"),
    StartTime(name = "referral-to-status"),
  )
  override val referralDate: LocalDate,

  @ApiModelProperty("The expected intervention start date")
  @EndTime(name = "referral-to-expected-start")
  @StartTime(name = "expected-date-range")
  override val expectedStartDate: LocalDate?,

  @ApiModelProperty("The expected intervention end date")
  @EndTime(name = "expected-date-range")
  @FieldGroup(FieldGroupType.DEPENDENT_ALL, "expectedStartDate")
  override val expectedEndDate: LocalDate?,

  @ApiModelProperty("The actual intervention start date")
  @field:PastOrPresent
  @EndTime(name = "referral-to-start")
  @StartTime(name = "date-range")
  override val startDate: LocalDate?,

  @ApiModelProperty("The actual intervention end date, an end date is required if outcome is provided")
  @field:PastOrPresent
  @EndTime(name = "date-range")
  @FieldGroup(FieldGroupType.DEPENDENT_ALL, "outcome", "startDate")
  override val endDate: LocalDate?,

  @ApiModelProperty("The length of the intervention, the units of this are determined by the NSI type selected")
  @field:Positive
  override val length: Long?,

  @ApiModelProperty("The status of the intervention")
  @field:NotBlank
  @field:Size(max = 20)
  override val status: String,

  @ApiModelProperty("The status date")
  @field:PastOrPresent
  @EndTime(name = "referral-to-status")
  override val statusDate: LocalDateTime,

  @ApiModelProperty("The outcome of the intervention. required if an end date is provided")
  @NotBlankWhenProvided
  @field:Size(max = 100)
  @FieldGroup(FieldGroupType.DEPENDENT_ALL, "endDate")
  override val outcome: String?,

  @ApiModelProperty("General notes")
  @field:Size(max = 4000)
  override val notes: String?,

  @field:Valid
  @ApiModelProperty("The active manager associated to this NSI")
  override val manager: UpdateNsiManager,
) : CreateOrUpdateNsi
