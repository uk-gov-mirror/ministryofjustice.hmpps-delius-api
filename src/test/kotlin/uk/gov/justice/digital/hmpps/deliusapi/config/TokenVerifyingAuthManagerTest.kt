package uk.gov.justice.digital.hmpps.deliusapi.config

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.deliusapi.config.wiremock.TokenVerificationExtension
import uk.gov.justice.digital.hmpps.deliusapi.config.wiremock.TokenVerificationExtension.Companion.tokenVerificationApi
import uk.gov.justice.digital.hmpps.deliusapi.config.wiremock.TokenVerificationMockServer
import wiremock.org.eclipse.jetty.http.HttpHeader

@ExtendWith(MockitoExtension::class, TokenVerificationExtension::class)
class TokenVerifyingAuthManagerTest {
  @Mock private lateinit var decoder: JwtDecoder
  @Mock private lateinit var converter: AuthAwareTokenConverter
  private lateinit var client: WebClient
  private lateinit var jwt: Jwt
  private lateinit var token: AuthAwareAuthenticationToken

  @BeforeEach
  fun beforeEach() {
    client = WebClient.create(TokenVerificationMockServer.URL)
    jwt = Jwt.withTokenValue("dummy-token-value")
      .claim(JwtClaimNames.SUB, "some-subject-id")
      .header("dummy-header", "dummy-header-value")
      .build()
    token = AuthAwareAuthenticationToken(jwt, authorities = emptyList(), 1)
  }

  @Test
  fun `Verifying token when token verification is disabled`() {
    havingTokenDecode()
    val observed = whenVerifyingToken(tokenVerificationEnabled = false, webClientWired = false)
    shouldReturnValidToken(observed)
    shouldAttemptTokenVerification(should = false)
  }

  @Test
  fun `Attempting to verify token when token verification is disabled but web client is wired`() {
    assertThrows<RuntimeException> {
      whenVerifyingToken(tokenVerificationEnabled = false, webClientWired = true)
    }
  }

  @Test
  fun `Attempting to verify token when token verification is enabled but web client is not wired`() {
    assertThrows<RuntimeException> {
      whenVerifyingToken(tokenVerificationEnabled = true, webClientWired = false)
    }
  }

  @Test
  fun `Verifying active token`() {
    havingTokenDecode()
    val observed = whenVerifyingToken()
    shouldReturnValidToken(observed)
    shouldAttemptTokenVerification()
  }

  @Test
  fun `Verifying inactive token`() {
    tokenVerificationApi.stubVerifyRequest(active = false)

    assertThrows<InvalidBearerTokenException> {
      whenVerifyingToken()
    }

    shouldAttemptTokenVerification()
  }

  private fun havingTokenDecode() {
    whenever(decoder.decode(jwt.tokenValue)).thenReturn(jwt)
    whenever(converter.convert(jwt)).thenReturn(token)
  }

  private fun whenVerifyingToken(
    tokenVerificationEnabled: Boolean = true,
    webClientWired: Boolean = true,
  ): Authentication {
    val features = FeatureFlags(tokenVerification = tokenVerificationEnabled)
    val subject = TokenVerifyingAuthManager(decoder, if (webClientWired) client else null, features, converter)
    val authentication = BearerTokenAuthenticationToken(jwt.tokenValue)
    return subject.authenticate(authentication)
  }

  private fun shouldReturnValidToken(observed: Authentication) {
    assertThat(observed).isSameAs(token)
  }

  private fun shouldAttemptTokenVerification(should: Boolean = true) {
    if (should) {
      tokenVerificationApi.verify(
        postRequestedFor(urlEqualTo("/token/verify"))
          .withHeader(HttpHeader.AUTHORIZATION.asString(), equalTo("Bearer ${jwt.tokenValue}"))
      )
    } else {
      tokenVerificationApi.verify(exactly(0), postRequestedFor(urlEqualTo("/token/verify")))
    }
  }
}
