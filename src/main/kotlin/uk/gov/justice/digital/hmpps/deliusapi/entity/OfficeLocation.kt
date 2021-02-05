package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "OFFICE_LOCATION")
data class OfficeLocation(
  @Id @Column(name = "OFFICE_LOCATION_ID")
  var id: Long = 0,

  @Column(name = "CODE")
  val code: String? = null
)
