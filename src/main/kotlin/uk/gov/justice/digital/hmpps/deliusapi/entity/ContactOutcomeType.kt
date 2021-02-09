package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_CONTACT_OUTCOME_TYPE")
data class ContactOutcomeType(
  @Id
  @Column(name = "CONTACT_OUTCOME_TYPE_ID")
  var id: Long,

  @Column(name = "CODE")
  var code: String
)
