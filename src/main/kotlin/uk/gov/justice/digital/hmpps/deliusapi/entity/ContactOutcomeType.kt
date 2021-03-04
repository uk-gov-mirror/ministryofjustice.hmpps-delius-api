package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_OUTCOME_TYPE")
@Where(clause = "SELECTABLE = 'Y'")
data class ContactOutcomeType(
  @Id
  @Column(name = "CONTACT_OUTCOME_TYPE_ID")
  var id: Long,

  @Column(name = "CODE")
  var code: String,

  @Column(name = "OUTCOME_COMPLIANT_ACCEPTABLE", columnDefinition = "CHAR(1)")
  @Type(type = "yes_no")
  val compliantAcceptable: Boolean?,

  @Column(name = "OUTCOME_ATTENDANCE", columnDefinition = "CHAR(1)")
  @Type(type = "yes_no")
  var attendance: Boolean?,
)
