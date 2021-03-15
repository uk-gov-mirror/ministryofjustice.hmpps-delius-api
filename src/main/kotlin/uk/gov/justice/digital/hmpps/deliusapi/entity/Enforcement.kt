package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "ENFORCEMENT")
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "SOFT_DELETED = 0")
data class Enforcement(
  @Id
  @SequenceGenerator(name = "ENFORCEMENT_ID_SEQ", sequenceName = "ENFORCEMENT_ID_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "ENFORCEMENT_ID_SEQ")
  @Column(name = "ENFORCEMENT_ID")
  var id: Long = 0,

  @Column(name = "RESPONSE_DATE")
  var responseDate: LocalDate? = null,

  @Column(name = "ACTION_TAKEN_DATE")
  var actionTakenDate: LocalDate? = null,

  @Column(name = "ACTION_TAKEN_TIME")
  var actionTakenTime: LocalTime? = null,

  @ManyToOne
  @JoinColumn(name = "ENFORCEMENT_ACTION_ID")
  var action: EnforcementAction? = null,

  @Column(name = "PARTITION_AREA_ID", nullable = false)
  var partitionAreaId: Long = 0,

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false,

  @Column(name = "ROW_VERSION", nullable = false)
  @Version
  var rowVersion: Long = 1,

  @Column(name = "CREATED_DATETIME", nullable = false)
  @CreatedDate
  var createdDateTime: LocalDateTime? = null,

  @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
  @LastModifiedDate
  var lastUpdatedDateTime: LocalDateTime? = null,

  @Column(name = "CREATED_BY_USER_ID", nullable = false)
  @CreatedBy
  var createdByUserId: Long = 0,

  @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
  @LastModifiedBy
  var lastUpdatedUserId: Long = 0,

  @ManyToOne
  @JoinColumn(name = "CONTACT_ID")
  var contact: Contact? = null,
)
