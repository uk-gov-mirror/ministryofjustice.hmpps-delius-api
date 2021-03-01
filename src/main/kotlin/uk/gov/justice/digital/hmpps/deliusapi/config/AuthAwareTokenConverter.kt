package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.deliusapi.service.security.DeliusSecurityService

@Component
class AuthAwareTokenConverter(
  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Value("\${auditing.default-staff-id}")
  private val defaultStaffId: Long,
  private val deliusSecurity: DeliusSecurityService,
) : Converter<Jwt, AbstractAuthenticationToken> {

  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val authSource = jwt.getClaimAsString(ClaimNames.AUTH_SOURCE)
    val userIdClaim = jwt.getClaimAsString(ClaimNames.USER_ID)?.toLong()
    val userId = if (userIdClaim != null && authSource == "delius") userIdClaim else defaultStaffId

    val externalAuthorities = jwt.claims.getOrDefault(ClaimNames.AUTHORITIES, emptyList<String>()) as? Collection<*>
      ?: emptyList<String>()
    val providerAuthorities = deliusSecurity.getGrantedProviders(userId).map { Authorities.PROVIDER + it.code }

    val allAuthorities = (externalAuthorities + providerAuthorities).mapNotNull {
      when (it) {
        is String -> SimpleGrantedAuthority(it)
        else -> null
      }
    }.toSet()

    return AuthAwareAuthenticationToken(jwt, allAuthorities, userId)
  }
}
