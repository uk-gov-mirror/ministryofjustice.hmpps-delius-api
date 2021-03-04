package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "RQMNT")
@Where(clause = "SOFT_DELETED = 0")
data class Requirement(
  @Id
  @Column(name = "RQMNT_ID")
  var id: Long,

  @Column(name = "TERMINATION_DATE")
  var terminationDate: LocalDate? = null,

  @Column(name = "OFFENDER_ID")
  var offenderId: Long,

  @Column(name = "ACTIVE_FLAG", columnDefinition = "NUMBER", nullable = false)
  var active: Boolean,

  @JoinColumn(name = "RQMNT_TYPE_MAIN_CATEGORY_ID")
  @ManyToOne
  var typeCategory: RequirementTypeCategory? = null,
)
