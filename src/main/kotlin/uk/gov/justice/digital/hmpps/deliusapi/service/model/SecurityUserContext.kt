package uk.gov.justice.digital.hmpps.deliusapi.service.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.deliusapi.config.AuthAwareAuthenticationToken

@Component
class SecurityUserContext(
  @Value("\${auditing.default-staff-id}") private val defaultStaffId: Long
) {
  private val authentication: AuthAwareAuthenticationToken
    get() = when (val token = SecurityContextHolder.getContext().authentication) {
      is AuthAwareAuthenticationToken -> token
      else -> throw RuntimeException("security context configuration not configured")
    }

  fun getCurrentDeliusUserId(): Long {
    val token = authentication
    return if (token.authSource == "delius") token.userId ?: defaultStaffId else defaultStaffId
  }
}
