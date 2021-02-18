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
@Table(name = "R_NSI_TYPE")
@Where(clause = "SELECTABLE = 'Y'")
data class NsiType(
  @Id
  @Column(name = "NSI_TYPE_ID", nullable = false)
  var id: Long = 0,

  @Column(name = "CODE", nullable = false)
  var code: String,

  @ManyToMany
  @JoinTable(
    name = "R_NSI_TYPE_SUB_TYPE",
    joinColumns = [JoinColumn(name = "NSI_TYPE_ID")],
    inverseJoinColumns = [JoinColumn(name = "NSI_SUB_TYPE_ID")],
  )
  var subTypes: List<StandardReference>? = null,
)
