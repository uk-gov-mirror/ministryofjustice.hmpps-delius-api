package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_TYPE")
@Where(clause = "SELECTABLE = 'Y'")
data class ContactType(
  @Id
  @Column(name = "CONTACT_TYPE_ID")
  var id: Long,

  @Column(name = "CODE")
  var code: String,

  @Column(name = "CONTACT_ALERT_FLAG")
  @Type(type = "yes_no")
  var contactAlertFlag: Boolean = false,

  @ManyToMany
  @JoinTable(
    name = "R_CONTACT_TYPE_OUTCOME",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID")],
  )
  var outcomeTypes: List<ContactOutcomeType>? = null
)
