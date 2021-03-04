package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_RQMNT_TYPE_MAIN_CATEGORY")
data class RequirementTypeCategory(
  @Id
  @Column(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
  var id: Long,

  @Column(name = "CODE", length = 20, nullable = false)
  var code: String,
)
