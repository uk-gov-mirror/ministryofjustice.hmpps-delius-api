package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
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
  var selectable: Boolean,

  @Column(name = "SPG_OVERRIDE", columnDefinition = "NUMBER")
  var spgOverride: Boolean?,

  @Column(name = "CONTACT_ALERT_FLAG")
  @Type(type = "yes_no")
  var alertFlag: Boolean,

  @Column(name = "CONTACT_OUTCOME_FLAG", columnDefinition = "CHAR(1)", nullable = false)
  @Enumerated(EnumType.STRING)
  var outcomeFlag: YesNoBoth,

  @Column(name = "CONTACT_LOCATION_FLAG", columnDefinition = "CHAR(1)", nullable = false)
  @Enumerated(EnumType.STRING)
  var locationFlag: YesNoBoth,

  @Column(name = "ATTENDANCE_CONTACT")
  @Type(type = "yes_no")
  var attendanceContact: Boolean,

  @Column(name = "RECORDED_HOURS_CREDITED")
  @Type(type = "yes_no")
  var recordedHoursCredited: Boolean,

  @Column(name = "CJA_ORDERS", nullable = false, length = 1)
  @Type(type = "yes_no")
  var cjaOrderLevel: Boolean,

  @Column(name = "LEGACY_ORDERS", nullable = false, length = 1)
  @Type(type = "yes_no")
  var legacyOrderLevel: Boolean,

  @Column(name = "OFFENDER_LEVEL_CONTACT", nullable = false, length = 1)
  @Type(type = "yes_no")
  var wholeOrderLevel: Boolean,

  @Column(name = "OFFENDER_EVENT_0", nullable = false, length = 1)
  @Type(type = "yes_no")
  var offenderLevel: Boolean,

  @Column(name = "FUTURE_SCHEDULED_CONTACTS_FLAG", nullable = false, length = 1)
  @Type(type = "yes_no")
  var scheduleFutureAppointments: Boolean,

  @Column(name = "EDITABLE", length = 1)
  @Type(type = "yes_no")
  var editable: Boolean?,

  @Column(name = "DEFAULT_HEADINGS")
  @Lob
  var defaultHeadings: String?,

  @ManyToMany
  @JoinTable(
    name = "R_CONTACT_TYPE_OUTCOME",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID", nullable = false)],
  )
  var outcomeTypes: List<ContactOutcomeType>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_CON_TYPE_REQ_TYPE_MAINCAT",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID", nullable = false)]
  )
  var requirementTypeCategories: List<RequirementTypeCategory>? = null,

  @ManyToMany
  @JoinTable(
    name = "R_CONTACT_TYPE_NSI_TYPE",
    joinColumns = [JoinColumn(name = "CONTACT_TYPE_ID", nullable = false)],
    inverseJoinColumns = [JoinColumn(name = "NSI_TYPE_ID", nullable = false)],
  )
  var nsiTypes: List<NsiType>? = null,

  @OneToMany
  @JoinColumn(name = "CONTACT_TYPE_ID")
  var enforcementActions: List<EnforcementAction>? = null,
)
