package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "NSI_STATUS_HISTORY")
@Where(clause = "SOFT_DELETED = 0")
class NsiStatusHistory(
  @Id
  @SequenceGenerator(name = "NSI_STATUS_HISTORY_ID_SEQ", sequenceName = "NSI_STATUS_HISTORY_ID_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NSI_STATUS_HISTORY_ID_SEQ")
  @Column(name = "NSI_STATUS_HISTORY_ID", nullable = false)
  var id: Long = 0,

  @JoinColumn(name = "NSI_ID", nullable = false)
  @ManyToOne
  var nsi: Nsi? = null,

  @JoinColumn(name = "NSI_STATUS_ID", nullable = false)
  @ManyToOne
  var nsiStatus: NsiStatus? = null,

  @Column(name = "NSI_STATUS_DATE")
  var date: LocalDateTime? = null,

  @Column(name = "NOTES")
  @Lob
  var notes: String?,

  @Column(name = "SOFT_DELETED", columnDefinition = "NUMBER", nullable = false)
  var softDeleted: Boolean = false,

  @Column(name = "ROW_VERSION", nullable = false)
  @Version
  var rowVersion: Long = 1,

  @Column(name = "CREATED_BY_USER_ID", nullable = false)
  @CreatedBy
  var createdByUserId: Long = 0,

  @Column(name = "CREATED_DATETIME", nullable = false)
  @CreatedDate
  var createdDateTime: LocalDateTime? = null,

  @Column(name = "LAST_UPDATED_USER_ID", nullable = false)
  @LastModifiedBy
  var lastUpdatedUserId: Long = 0,

  @Column(name = "LAST_UPDATED_DATETIME", nullable = false)
  @LastModifiedDate
  var lastUpdatedDateTime: LocalDateTime? = null,
)
