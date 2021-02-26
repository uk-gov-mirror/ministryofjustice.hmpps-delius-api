package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_TYPE")
data class ContactType(
  @Id
  @Column(name = "CONTACT_TYPE_ID")
  var id: Long,

  @Column(name = "CODE", nullable = false)
  var code: String,

  @Column(name = "SELECTABLE", nullable = false)
  @Type(type = "yes_no")
  var selectable: Boolean = false,

  @Column(name = "SPG_OVERRIDE", columnDefinition = "NUMBER", nullable = false)
  var spgOverride: Boolean = false,

  @Column(name = "CONTACT_ALERT_FLAG")
  @Type(type = "yes_no")
  var alertFlag: Boolean = false,

  @Column(name = "CONTACT_OUTCOME_FLAG", columnDefinition = "CHAR(1)", nullable = false)
  @Enumerated(EnumType.STRING)
  var outcomeFlag: YesNoBoth = YesNoBoth.N,

  @Column(name = "CONTACT_LOCATION_FLAG", columnDefinition = "CHAR(1)", nullable = false)
  @Enumerated(EnumType.STRING)
  var locationFlag: YesNoBoth = YesNoBoth.N,

  @Column(name = "ATTENDANCE_CONTACT")
  @Type(type = "yes_no")
  var attendanceContact: Boolean = false,

  @Column(name = "RECORDED_HOURS_CREDITED")
  @Type(type = "yes_no")
  var recordedHoursCredited: Boolean = false,

  @ManyToMany
  @JoinTable(
    name = "R_CONTACT_TYPE_OUTCOME",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID")],
  )
  var outcomeTypes: List<ContactOutcomeType>? = null
)
