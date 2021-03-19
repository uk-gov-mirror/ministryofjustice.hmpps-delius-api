package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_DISPOSAL_TYPE")
class DisposalType(
  @Id
  @Column(name = "DISPOSAL_TYPE_ID")
  var id: Long,

  @Column(name = "CJA2003")
  @Type(type = "yes_no")
  var cja2003Order: Boolean,

  @Column(name = "PRE_CJA2003")
  @Type(type = "yes_no")
  var legacyOrder: Boolean,

  @Column(name = "SENTENCE_TYPE", length = 50)
  var sentenceType: String? = null,

  @Column(name = "FTC_LIMIT")
  var failureToComplyLimit: Long? = null,
)
