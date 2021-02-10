package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "TEAM")
@Where(clause = "END_DATE IS NULL OR END_DATE > CURRENT_DATE()")
data class Team(
  @Id
  @Column(name = "TEAM_ID")
  var id: Long,

  @Column(name = "CODE")
  var code: String,

  @ManyToMany
  @JoinTable(
    name = "STAFF_TEAM",
    joinColumns = [JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")],
    inverseJoinColumns = [JoinColumn(name = "STAFF_ID", referencedColumnName = "STAFF_ID")],
  )
  var staff: List<Staff>? = null
)
