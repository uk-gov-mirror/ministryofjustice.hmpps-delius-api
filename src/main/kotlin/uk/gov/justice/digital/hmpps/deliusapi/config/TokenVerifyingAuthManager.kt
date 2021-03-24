package uk.gov.justice.digital.hmpps.deliusapi.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TokenVerifyingAuthManager(
  jwtDecoderByIssuerUri: JwtDecoder,
  @Autowired(required = false) private val tokenVerificationApiWebClient: WebClient?,
  features: FeatureFlags,
  authAwareTokenConverter: AuthAwareTokenConverter,
) :
  AuthenticationManager {

  private val jwtAuthenticationProvider = JwtAuthenticationProvider(jwtDecoderByIssuerUri)

  init {
    jwtAuthenticationProvider.setJwtAuthenticationConverter(authAwareTokenConverter)

    if (features.tokenVerification != (tokenVerificationApiWebClient != null)) {
      throw RuntimeException("Token verification API client must only be wired when token verification feature is enabled")
    }
  }

  override fun authenticate(authentication: Authentication): Authentication {
    if (tokenVerificationApiWebClient != null) {
      val bearer = authentication as BearerTokenAuthenticationToken

      // firstly check the jwt is still valid
      val tokenDto = tokenVerificationApiWebClient.post().uri("/token/verify")
        .header(HttpHeaders.AUTHORIZATION, "Bearer ${bearer.token}")
        .retrieve()
        .bodyToMono(TokenDto::class.java)
        .blockOptional()
      val tokenActive = tokenDto.map { it.active }.orElse(false)

      // can't proceed if the token is then not active
      if (!tokenActive) throw InvalidBearerTokenException("Token verification failed")
    }

    return jwtAuthenticationProvider.authenticate(authentication)
  }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TokenDto(val active: Boolean)
