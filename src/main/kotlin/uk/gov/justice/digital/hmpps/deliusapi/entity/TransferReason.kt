package uk.gov.justice.digital.hmpps.deliusapi.entity

import org.hibernate.annotations.Where
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_TRANSFER_REASON")
@Where(clause = "SELECTABLE = 'Y'")
class TransferReason(
  @Id
  @Column(name = "TRANSFER_REASON_ID")
  var id: Long,

  @Column(name = "CODE", length = 100, nullable = false)
  var code: String,
)
