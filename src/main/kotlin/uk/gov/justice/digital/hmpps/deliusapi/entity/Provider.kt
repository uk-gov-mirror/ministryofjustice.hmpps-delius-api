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
@Where(clause = "SELECTABLE = 'Y'")
data class Provider(
  @Id
  @Column(name = "PROBATION_AREA_ID")
  var id: Long,

  @Column(name = "CODE", columnDefinition = "CHAR(3)")
  val code: String,

  @OneToMany
  @JoinColumn(name = "PROBATION_AREA_ID")
  val officeLocations: List<OfficeLocation>? = null,
)
