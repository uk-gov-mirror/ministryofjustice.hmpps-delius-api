package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider

@Repository
interface ProviderRepository : JpaRepository<Provider, Long> {
  fun findByCodeAndSelectableIsTrue(code: String): Provider?
}
