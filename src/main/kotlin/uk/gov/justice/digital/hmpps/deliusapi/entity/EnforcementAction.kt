package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_ENFORCEMENT_ACTION")
@Where(clause = "SELECTABLE = 'Y'")
data class EnforcementAction(
  @Id
  @Column(name = "ENFORCEMENT_ACTION_ID", nullable = false)
  var id: Long,

  @Column(name = "CODE", length = 10, nullable = false)
  var code: String,

  @Column(name = "OUTSTANDING_CONTACT_ACTION")
  @Type(type = "yes_no")
  var outstandingContactAction: Boolean?,

  @Column(name = "RESPONSE_BY_PERIOD")
  var responseByPeriod: Long?,
)
