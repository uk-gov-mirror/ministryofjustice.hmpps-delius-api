package uk.gov.justice.digital.hmpps.deliusapi.service.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.deliusapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.deliusapi.config.Authorities

@Component
class SecurityUserContext {
  private val authentication: AuthAwareAuthenticationToken
    get() = when (val token = SecurityContextHolder.getContext().authentication) {
      is AuthAwareAuthenticationToken -> token
      else -> throw RuntimeException("security context configuration not configured")
    }

  fun getCurrentDeliusUserId() = authentication.userId

  fun assertProviderAuthority(provider: String) {
    val providerAuthority = Authorities.PROVIDER + provider
    if (!authentication.authorities.any { it.authority == providerAuthority }) {
      throw AccessDeniedException("No access to provider with code '$provider'")
    }
  }
}
