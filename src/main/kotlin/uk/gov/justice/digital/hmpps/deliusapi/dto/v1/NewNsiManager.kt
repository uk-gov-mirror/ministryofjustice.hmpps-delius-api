package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.deliusapi.validation.ProviderCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StaffCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.TeamCode

data class NewNsiManager(
  /**
   * TODO must be existing, selectable (via start & end date) staff
   * TODO must be in specified managerTeam
   */
  @ApiModelProperty("The officer code of the staff managing this intervention")
  @field:StaffCode
  val staff: String,

  /**
   * TODO must be existing, selectable (via start & end date) team
   * TODO must be in specified managerProvider
   */

  @ApiModelProperty("The code of the team managing this intervention")
  @field:TeamCode
  val team: String,

  /**
   * TODO must be existing, selectable (via selectable flag) provider
   */
  @ApiModelProperty("The code of the provider managing this intervention")
  @field:ProviderCode
  val provider: String,
)
