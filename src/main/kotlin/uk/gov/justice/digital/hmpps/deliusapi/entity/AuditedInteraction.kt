package uk.gov.justice.digital.hmpps.deliusapi.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "AUDITED_INTERACTION")
data class AuditedInteraction(
  @Id
  @Column(name = "DATE_TIME")
  var dateTime: LocalDateTime?,

  @Column(nullable = false, columnDefinition = "CHAR(1)")
  var outcome: String,

  @Column(name = "INTERACTION_PARAMETERS", length = 500)
  var parameters: String,

  @ManyToOne
  @JoinColumn(name = "BUSINESS_INTERACTION_ID")
  var businessInteraction: BusinessInteraction,

  @Column(name = "USER_ID")
  var userID: Long,
)
