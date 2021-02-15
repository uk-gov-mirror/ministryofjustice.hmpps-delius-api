package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "EVENT")
@Where(clause = "SOFT_DELETED = 0")
data class Event(
  @Id
  @Column(name = "EVENT_ID")
  var id: Long,

  @JoinColumn(name = "EVENT_ID")
  @OneToMany
  var disposals: List<Disposal>? = null,
)
