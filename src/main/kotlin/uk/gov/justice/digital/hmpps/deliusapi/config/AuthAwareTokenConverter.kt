package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class AuthAwareTokenConverter :
  Converter<Jwt, AbstractAuthenticationToken> {

  override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    return AuthAwareAuthenticationToken(jwt, extractAuthorities(jwt))
  }

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
    val authorities = jwt.claims.getOrDefault(ClaimNames.AUTHORITIES, listOf<String>()) as? Collection<*>
    return authorities?.mapNotNull { role ->
      if (role is String) SimpleGrantedAuthority(role) else null
    }?.toSet() ?: setOf()
  }
}
