package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_STANDARD_REFERENCE_LIST")
@Where(clause = "SELECTABLE = 'Y'")
data class StandardReference(
  @Id
  @Column(name = "STANDARD_REFERENCE_LIST_ID")
  var id: Long,

  @Column(name = "CODE_VALUE")
  var code: String,
)