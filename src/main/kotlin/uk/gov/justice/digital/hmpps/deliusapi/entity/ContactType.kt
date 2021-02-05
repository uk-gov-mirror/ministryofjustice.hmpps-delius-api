package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_TYPE")
data class ContactType(
  @Id
  @Column(name = "CONTACT_TYPE_ID")
  var id: Long = 0,

  @Column(name = "CODE")
  var code: String? = null,
)
