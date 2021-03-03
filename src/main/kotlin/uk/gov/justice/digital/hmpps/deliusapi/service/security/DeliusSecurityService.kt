package uk.gov.justice.digital.hmpps.deliusapi.service.security

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderUserRepository

@Service
class DeliusSecurityService(private val providerUserRepository: ProviderUserRepository) {
  fun getGrantedProviders(userId: Long): List<Provider> =
    providerUserRepository.findAllByIdUserId(userId)
      .mapNotNull { it.provider }
      .filter { it.selectable }
}
