package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.ReferenceDataMaster

@Repository
interface ReferenceDataMasterRepository : JpaRepository<ReferenceDataMaster, Long> {
  fun findByCode(code: String): ReferenceDataMaster?
}

fun ReferenceDataMasterRepository.findByCodeOrThrow(code: String) =
  this.findByCode(code) ?: throw RuntimeException("Reference master with code '$code' does not exist")
