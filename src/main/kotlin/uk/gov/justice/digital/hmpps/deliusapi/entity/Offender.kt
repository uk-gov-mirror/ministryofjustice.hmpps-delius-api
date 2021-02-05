package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "OFFENDER")
data class Offender(
  @Id
  @Column(name = "OFFENDER_ID")
  var id: Long = 0,
)
