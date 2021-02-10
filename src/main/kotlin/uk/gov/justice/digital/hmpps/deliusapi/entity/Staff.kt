package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "STAFF")
@Where(clause = "END_DATE IS NULL OR END_DATE > CURRENT_DATE()")
data class Staff(
  @Id
  @Column(name = "STAFF_ID")
  var id: Long,

  @Column(name = "OFFICER_CODE")
  val code: String,
)
