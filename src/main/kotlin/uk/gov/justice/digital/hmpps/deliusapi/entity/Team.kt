package uk.gov.justice.digital.hmpps.deliusapi.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "TEAM")
data class Team(
  @Id
  @Column(name = "TEAM_ID")
  var id: Long,

  @Column(name = "CODE")
  var code: String,
)
