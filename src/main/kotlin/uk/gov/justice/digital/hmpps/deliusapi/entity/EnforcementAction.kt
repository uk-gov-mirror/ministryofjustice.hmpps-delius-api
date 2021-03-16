package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "R_ENFORCEMENT_ACTION")
@Where(clause = "SELECTABLE = 'Y'")
class EnforcementAction(
  @Id
  @Column(name = "ENFORCEMENT_ACTION_ID", nullable = false)
  var id: Long,

  @Column(name = "CODE", length = 10, nullable = false)
  var code: String,

  @Column(name = "DESCRIPTION", length = 50, nullable = false)
  var description: String,

  @Column(name = "OUTSTANDING_CONTACT_ACTION")
  @Type(type = "yes_no")
  var outstandingContactAction: Boolean?,

  @Column(name = "RESPONSE_BY_PERIOD")
  var responseByPeriod: Long?,

  @ManyToOne
  @JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)
  var contactType: ContactType,
)
