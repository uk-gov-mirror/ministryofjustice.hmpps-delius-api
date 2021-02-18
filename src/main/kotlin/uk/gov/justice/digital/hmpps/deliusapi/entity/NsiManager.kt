package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "NSI_MANAGER")
@Where(clause = "SOFT_DELETED = 0")
data class NsiManager(
  @Id
  @Column(name = "NSI_MANAGER_ID", nullable = false)
  var id: Long = 0,

  @Column(name = "START_DATE", nullable = false)
  var startDate: LocalDate,

  @Column(name = "END_DATE")
  var endDate: LocalDate? = null,

  @JoinColumn(name = "PROBATION_AREA_ID", nullable = false)
  @ManyToOne
  var provider: Provider? = null,

  @JoinColumn(name = "TEAM_ID", nullable = false)
  @ManyToOne
  var team: Team? = null,

  @JoinColumn(name = "STAFF_ID", nullable = false)
  @ManyToOne
  var staff: Staff? = null,

  @JoinColumn(name = "PARTITION_AREA_ID", nullable = false)
  @ManyToOne
  var partitionArea: PartitionArea? = null,

  @Column(name = "ACTIVE_FLAG")
  var active: Boolean = true,

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
)
