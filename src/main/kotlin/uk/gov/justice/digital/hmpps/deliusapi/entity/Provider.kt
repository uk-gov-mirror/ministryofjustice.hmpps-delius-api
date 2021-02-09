package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "PROBATION_AREA")
data class Provider(
  @Id
  @Column(name = "PROBATION_AREA_ID")
  var id: Long,

  @Column(name = "CODE")
  val code: String,
)
