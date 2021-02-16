package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.deliusapi.entity.BusinessInteraction

interface BusinessInteractionRepository : JpaRepository<BusinessInteraction, Long> {
  fun findFirstByCode(code: String): BusinessInteraction?
}
