package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByCrn(crn: String): Offender?
}

fun OffenderRepository.findByCrnOrBadRequest(crn: String): Offender =
  this.findByCrn(crn) ?: throw BadRequestException("Offender with code '$crn' does not exist")
