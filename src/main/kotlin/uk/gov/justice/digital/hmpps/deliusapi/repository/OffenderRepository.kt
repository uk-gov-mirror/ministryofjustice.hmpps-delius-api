package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import java.util.Optional

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByCrn(crn: String): Optional<Offender>
}
