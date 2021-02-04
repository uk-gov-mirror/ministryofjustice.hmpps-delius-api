package uk.gov.justice.digital.hmpps.deliusapi.config

import com.github.tomakehurst.wiremock.client.WireMock.*
import wiremock.org.eclipse.jetty.http.HttpHeader
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions
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

@ExtendWith(MockitoExtension::class, TokenVerificationExtension::class)
class TokenVerifyingAuthManagerTest {
  @Mock
  private lateinit var decoder: JwtDecoder
  private lateinit var client: WebClient
  private lateinit var token: Jwt

  @BeforeEach
  fun beforeEach() {
    token = Jwt.withTokenValue("dummy-token-value")
      .claim(JwtClaimNames.SUB,"some-subject-id")
      .header("dummy-header", "dummy-header-value")
      .build()
    client = WebClient.create(TokenVerificationMockServer.URL)
  }

  @Test
  fun `Verifying token when token verification is disabled`() {
    havingTokenDecode()
    val observed = whenVerifyingToken(tokenVerificationEnabled = false)
    shouldReturnValidToken(observed)
    shouldAttemptTokenVerification(should = false)
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
    whenever(decoder.decode(token.tokenValue)).thenReturn(token)
  }

  private fun whenVerifyingToken(tokenVerificationEnabled: Boolean = true): Authentication {
    val subject = TokenVerifyingAuthManager(decoder, client, tokenVerificationEnabled)
    val authentication = BearerTokenAuthenticationToken(token.tokenValue)
    return subject.authenticate(authentication)
  }

  private fun shouldReturnValidToken(observed: Authentication) {
    Assertions.assertThat(observed).isNotNull.isInstanceOf(AuthAwareAuthenticationToken::class.java)
  }

  private fun shouldAttemptTokenVerification(should: Boolean = true) {
    if (should) {
      tokenVerificationApi.verify(
        postRequestedFor(urlEqualTo("/token/verify"))
          .withHeader(HttpHeader.AUTHORIZATION.asString(), equalTo("Bearer ${token.tokenValue}"))
      )
    } else {
      tokenVerificationApi.verify(exactly(0), postRequestedFor(urlEqualTo("/token/verify")))
    }
  }
}