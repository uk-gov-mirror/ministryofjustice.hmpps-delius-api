package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.TransferReason

@Repository
interface TransferReasonRepository : JpaRepository<TransferReason, Long> {
  fun findByCode(code: String): TransferReason?
}
