package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "PROBATION_AREA")
@Where(clause = "END_DATE IS NULL OR END_DATE > CURRENT_DATE()")
data class Provider(
  @Id
  @Column(name = "PROBATION_AREA_ID")
  var id: Long,

  @Column(name = "CODE")
  val code: String,

  @OneToMany
  @JoinColumn(name = "PROBATION_AREA_ID")
  val officeLocations: List<OfficeLocation>? = null
)
