package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_STANDARD_REFERENCE_LIST")
@Where(clause = "SELECTABLE = 'Y'")
class StandardReference(
  @Id
  @Column(name = "STANDARD_REFERENCE_LIST_ID", nullable = false)
  var id: Long,

  @Column(name = "CODE_VALUE", length = 100, nullable = false)
  var code: String,

  @Column(name = "CODE_DESCRIPTION", length = 500, nullable = false)
  var description: String,
)
