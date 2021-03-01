package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.deliusapi.entity.ProviderUser
import uk.gov.justice.digital.hmpps.deliusapi.entity.ProviderUserIdentity

interface ProviderUserRepository : JpaRepository<ProviderUser, ProviderUserIdentity> {
  fun findAllByIdUserId(userId: Long): List<ProviderUser>
}
