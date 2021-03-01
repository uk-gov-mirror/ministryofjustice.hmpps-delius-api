package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_NSI_STATUS")
@Where(clause = "SELECTABLE = 'Y'")
data class NsiStatus(
  @Id
  @Column(name = "NSI_STATUS_ID")
  var id: Long,

  @Column(name = "CODE")
  var code: String,

  @Column(name = "CONTACT_TYPE_ID")
  var contactTypeId: Long,
)
