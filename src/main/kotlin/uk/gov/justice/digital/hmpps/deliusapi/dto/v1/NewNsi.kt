package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

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
  /**
   * TODO must be an existing nsi type
   * TODO selectable flag set
   * TODO check presence of offender & event against offender level & event level flag
   */
  @ApiModelProperty("The type of the NSI")
  @field:NsiTypeCode
  val type: String,

  /**
   * TODO must be an existing nsi type
   * TODO selectable flag set
   * TODO must be a valid sub type of the specified type
   * TODO required when specified nsi type has >0 selectable sub types
   */
  @ApiModelProperty("The sub type of the NSI, this is required for some NSI types")
  @field:NsiTypeCode
  val subType: String? = null,

  /**
   * TODO existing, non-soft deleted offender
   */
  @ApiModelProperty("The offender CRN")
  @field:Crn
  val offenderCrn: String,

  /**
   * TODO must be an existing, non-soft deleted event associated to the offender
   */
  @ApiModelProperty("An optional event ID that the new NSI will be associated to")
  @field:Positive
  val eventId: Long? = null,

  /**
   * TODO must be an existing, non-soft deleted requirement associated to the offender & event
   */
  @ApiModelProperty("An optional requirement ID that the new NSI will be associated to, an event is required for association to a requirement")
  @field:Positive
  @field:DependentFields("eventId")
  val requirementId: Long? = null,

  /**
   * TODO if event is specified then must be after or equal to the referral date of the event
   */
  @ApiModelProperty("The date of the referral")
  @field:PastOrPresent
  @field:StartTimes(
    StartTime(name = "referral-to-expected-start"),
    StartTime(name = "referral-to-start"),
    StartTime(name = "referral-to-status"),
  )
  val referralDate: LocalDate,

  @ApiModelProperty("The expected intervention start date")
  @field:EndTime(name = "referral-to-expected-start")
  @field:StartTime(name = "expected-date-range")
  val expectedStartDate: LocalDate?,

  @ApiModelProperty("The expected intervention end date")
  @field:EndTime(name = "expected-date-range")
  @field:DependentFields("expectedStartDate")
  val expectedEndDate: LocalDate?,

  @ApiModelProperty("The actual intervention start date.")
  @field:PastOrPresent
  @field:EndTime(name = "referral-to-start")
  @field:StartTime(name = "date-range")
  val startDate: LocalDate?,

  /**
   * TODO also required if a requirement is specified with a termination date (RQMNT.TERMINATION_DATE)
   * TODO if a requirement is specified with a termination date (RQMNT.TERMINATION_DATE) then endDate must be before or equal to termination date
   */
  @ApiModelProperty("The actual intervention end date, an end date is required if outcome is provided")
  @field:PastOrPresent
  @field:EndTime(name = "date-range")
  @field:DependentFields("outcome", "startDate")
  val endDate: LocalDate?,

  /**
   * TODO required when the type selected has associated UNITS (R_NSI_TYPE.UNITS_ID IS NOT NULL) otherwise must not be provided
   * TODO must be larger than (exclusive ???) minimum length (R_NSI_TYPE.MINIMUM_LENGTH)
   * TODO must be smaller than (also exclusive ???) maximum length (R_NSI_TYPE.MAXIMUM_LENGTH)
   */
  @ApiModelProperty("The length of the intervention, the units of this are determined by the NSI type selected")
  @field:Positive
  val length: Long?,

  /**
   * TODO must be an existing nsi status
   * TODO selectable flag set
   * TODO must be associated with selected nsi type (via R_NSI_TYPE_STATUS)
   */
  @ApiModelProperty("The status of the intervention")
  @field:NotBlank
  @field:Size(max = 20)
  val status: String,

  @ApiModelProperty("The status date")
  @field:PastOrPresent
  @field:EndTime(name = "referral-to-status")
  val statusDate: LocalDateTime,

  /**
   * TODO must be an existing standard reference
   * TODO selectable flag set
   * TODO must be associated with selected nsi type (via R_NSI_TYPE_OUTCOME)
   */
  @ApiModelProperty("The outcome of the intervention. required if an end date is provided")
  @field:NotBlankWhenProvided
  @field:Size(max = 100)
  @field:DependentFields("endDate")
  val outcome: String?,

  @ApiModelProperty("General notes")
  @field:Size(max = 4000)
  val notes: String?,

  /**
   * TODO must be existing, selectable provider
   * TODO must be associated with selected nsi type (via R_NSI_TYPE_PROBATION_AREA)
   */
  @ApiModelProperty("The provider")
  @field:ProviderCode
  val intendedProvider: String,

  @ApiModelProperty("The active manager associated to this NSI")
  val manager: NewNsiManager,
)
