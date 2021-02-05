package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@Table(name = "CONTACT")
data class Contact(
  @Id
  @SequenceGenerator(name = "CONTACT_ID_GENERATOR", sequenceName = "CONTACT_ID_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_ID_GENERATOR")
  @Column(name = "CONTACT_ID", nullable = false)
  var id: Long = 0,

  // @Column(name = "LINKED_CONTACT_ID")
  // var linkedContactId: Long? = null,

  @Column(name = "CONTACT_DATE", nullable = false)
  var contactDate: LocalDate? = null,

  @JoinColumn(name = "OFFENDER_ID", nullable = false)
  @ManyToOne
  val offender: Offender? = null,

  @Column(name = "CONTACT_START_TIME")
  var contactStartTime: LocalTime? = null,

  @Column(name = "CONTACT_END_TIME")
  var contactEndTime: LocalTime? = null,

  // @Column(name = "RQMNT_ID")
  // var requirementId: Long? = null,

  // @Column(name = "LIC_CONDITION_ID")
  // var licenceConditionId: Long? = null,

  // @Column(name = "PROVIDER_LOCATION_ID")
  // var providerLocationId: Long? = null,

  // @Column(name = "PROVIDER_EMPLOYEE_ID")
  // var providerEmployeeId: Long? = null,

  // @Column(name = "HOURS_CREDITED")
  // var hoursCredited: Double? = null,

  @Column(name = "NOTES")
  var notes: String? = null,

  // @Column(name = "VISOR_CONTACT")
  // @Type(type = "yes_no")
  // var visorContact: Boolean? = false,

  @JoinColumn(name = "STAFF_ID")
  @ManyToOne
  var staff: Staff? = null,

  @JoinColumn(name = "TEAM_ID")
  @ManyToOne
  var team: Team? = null,

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false,

  // @Column(name = "VISOR_EXPORTED")
  // @Type(type = "yes_no")
  // var visorExported: Boolean? = false,

  @Column(name = "PARTITION_AREA_ID", nullable = false)
  var partitionAreaId: Long = 0,

  @JoinColumn(name = "OFFICE_LOCATION_ID")
  @ManyToOne
  var officeLocation: OfficeLocation? = null,

  @Column(name = "ROW_VERSION", nullable = false)
  var rowVersion: Long = 0,

  @Column(name = "ALERT_ACTIVE")
  @Type(type = "yes_no")
  var alert: Boolean = false,

  // @Column(name = "ATTENDED")
  // @Type(type = "yes_no")
  // var attended: Boolean = false,

  @Column(name = "CREATED_DATETIME", nullable = false)
  @CreatedDate
  var createdDateTime: LocalDateTime? = null,

  // @Column(name = "COMPLIED")
  // @Type(type = "yes_no")
  // var complied: Boolean = false,

  @Column(name = "SENSITIVE")
  @Type(type = "yes_no")
  var sensitive: Boolean = false,

  @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
  @LastModifiedDate
  var lastUpdatedDateTime: LocalDateTime? = null,

  // @Column(name = "EVENT_ID")
  // var eventId: Long? = null,

  @JoinColumn(name = "CONTACT_TYPE_ID")
  @ManyToOne
  var contactType: ContactType? = null,

  // @Column(name = "PROVIDER_TEAM_ID")
  // var providerTeamId: Long? = null,

  @JoinColumn(name = "CONTACT_OUTCOME_TYPE_ID")
  @ManyToOne
  var contactOutcomeType: ContactOutcomeType? = null,

  @Column(name = "CREATED_BY_USER_ID", nullable = false)
  @CreatedBy
  var createdByUserId: Long = 0,

  // @Column(name = "EXPLANATION_ID")
  // var explanationId: Long? = null,

  @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
  @LastModifiedBy
  var lastUpdatedUserId: Long = 0,

  // @Column(name = "TRAINING_SESSION_ID")
  // var trainingSessionId: Long? = null,

  @Column(name = "TRUST_PROVIDER_FLAG", nullable = false)
  var trustProviderFlag: Long = 0,

  @Column(name = "STAFF_EMPLOYEE_ID", nullable = false)
  var staffEmployeeId: Long = 0,

  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider? = null,

  @Column(name = "TRUST_PROVIDER_TEAM_ID", nullable = false)
  var teamProviderId: Long = 0,

  // @Column(name = "ENFORCEMENT")
  // var enforcement: Long? = null,

  // @Column(name = "DOCUMENT_LINKED")
  // @Type(type = "yes_no")
  // var documentLinked: Boolean? = null,

  // @Column(name = "UPLOAD_LINKED")
  // @Type(type = "yes_no")
  // var uploadLinked: Boolean? = null,

  // @Column(name = "NSI_ID")
  // var nsiId: Long? = null,
)
