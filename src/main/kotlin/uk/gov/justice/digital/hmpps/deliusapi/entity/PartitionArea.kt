package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "PARTITION_AREA")
data class PartitionArea(
  @Id
  @Column(name = "PARTITION_AREA_ID", nullable = false)
  var id: Long = 0,

  @Column(name = "AREA", nullable = false, length = 30)
  var area: String,
)
