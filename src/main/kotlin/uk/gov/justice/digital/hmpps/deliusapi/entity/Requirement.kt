package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "RQMNT")
@Where(clause = "SOFT_DELETED = 0")
data class Requirement(
  @Id
  @Column(name = "RQMNT_ID")
  var id: Long,
)
