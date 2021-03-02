package uk.gov.justice.digital.hmpps.deliusapi.dto.v1

data class NsiManagerDto(
  val id: Long,
  val provider: String,
  val team: String? = null,
  val staff: String? = null,
)
