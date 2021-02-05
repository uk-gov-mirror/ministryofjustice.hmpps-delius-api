package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "STAFF")
data class Staff(
  @Id
  @Column(name = "STAFF_ID")
  var id: Long? = null,

  @Column(name = "OFFICER_CODE")
  val code: String? = null,
)
