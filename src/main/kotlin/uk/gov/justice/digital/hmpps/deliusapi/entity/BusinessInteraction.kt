package uk.gov.justice.digital.hmpps.deliusapi.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "BUSINESS_INTERACTION")
data class BusinessInteraction(
  @Id
  @Column(name = "BUSINESS_INTERACTION_ID", unique = true, nullable = false, precision = 38)
  var id: Long,

  @Column(name = "BUSINESS_INTERACTION_CODE", nullable = false, length = 20)
  var code: String,

  @Column(length = 50)
  var description: String,

  @Column(name = "ENABLED_DATE")
  var enabledDate: LocalDateTime?
)
