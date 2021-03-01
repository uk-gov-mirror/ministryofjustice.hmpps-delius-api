package uk.gov.justice.digital.hmpps.deliusapi.service.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.deliusapi.config.AuthAwareAuthenticationToken

@Component
class SecurityUserContext {
  private val authentication: AuthAwareAuthenticationToken
    get() = when (val token = SecurityContextHolder.getContext().authentication) {
      is AuthAwareAuthenticationToken -> token
      else -> throw RuntimeException("security context configuration not configured")
    }

  fun getCurrentDeliusUserId() = authentication.userId
}
