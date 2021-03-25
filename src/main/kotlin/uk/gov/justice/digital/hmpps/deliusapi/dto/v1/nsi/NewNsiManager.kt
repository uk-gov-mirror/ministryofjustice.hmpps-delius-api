package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi

import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.deliusapi.validation.ProviderCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.StaffCode
import uk.gov.justice.digital.hmpps.deliusapi.validation.TeamCode

data class NewNsiManager(
  @ApiModelProperty("The code of the provider managing this intervention")
  @ProviderCode
  val provider: String,

  @ApiModelProperty("The code of the team managing this intervention")
  @TeamCode
  override val team: String?,

  @ApiModelProperty("The officer code of the staff managing this intervention")
  @StaffCode
  override val staff: String?,
) : CreateOrUpdateNsiManager
