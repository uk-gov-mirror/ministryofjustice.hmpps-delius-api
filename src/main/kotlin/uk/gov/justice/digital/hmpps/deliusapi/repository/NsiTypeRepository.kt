package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

@Repository
interface NsiTypeRepository : JpaRepository<NsiType, Long> {
  fun findByCode(code: String): NsiType?
}

fun NsiTypeRepository.findByCodeOrBadRequest(code: String): NsiType =
  findByCode(code) ?: throw BadRequestException("NSI type with code '$code' does not exist")
