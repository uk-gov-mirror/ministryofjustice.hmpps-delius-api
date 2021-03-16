package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "AUDITED_INTERACTION")
class AuditedInteraction(
  @Id
  @Column(name = "DATE_TIME")
  @GeneratedValue(generator = "date")
  @GenericGenerator(name = "date", strategy = "uk.gov.justice.digital.hmpps.deliusapi.generator.DateTimeGenerator")
  var dateTime: LocalDateTime?,

  @Column(name = "OUTCOME", nullable = false, columnDefinition = "CHAR(1)")
  @Type(type = "uk.gov.justice.digital.hmpps.deliusapi.type.PassFailType")
  var success: Boolean,

  @Column(name = "INTERACTION_PARAMETERS", length = 500)
  var parameters: String,

  @ManyToOne
  @JoinColumn(name = "BUSINESS_INTERACTION_ID")
  var businessInteraction: BusinessInteraction,

  @Column(name = "USER_ID")
  var userId: Long,
)
