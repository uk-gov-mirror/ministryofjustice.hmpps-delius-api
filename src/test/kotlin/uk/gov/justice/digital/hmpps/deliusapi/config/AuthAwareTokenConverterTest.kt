package uk.gov.justice.digital.hmpps.deliusapi.config

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import uk.gov.justice.digital.hmpps.deliusapi.service.security.DeliusSecurityService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

@ExtendWith(MockitoExtension::class)
class AuthAwareTokenConverterTest {
  @Mock private lateinit var deliusSecurity: DeliusSecurityService
  private lateinit var subject: AuthAwareTokenConverter

  @BeforeEach
  fun beforeEach() {
    subject = AuthAwareTokenConverter(200L, deliusSecurity)
  }

  @Test
  fun `Converting subject token`() {
    val provider = Fake.provider()
    whenever(deliusSecurity.getGrantedProviders(100)).thenReturn(listOf(provider))
    val observed = whenConverting(aToken())

    assertThat(observed)
      .hasProperty(AuthAwareAuthenticationToken::subject, "some-subject-id")
      .describedAs("should set correct authorities")
      .returns(listOf("auth1", "auth2", Authorities.PROVIDER + provider.code)) {
        it.authorities?.map { a -> a.authority }
      }
  }

  private fun whenConverting(jwt: Jwt) = subject.convert(jwt) as AuthAwareAuthenticationToken

  private fun aToken(authSource: String = "delius"): Jwt {
    return Jwt.withTokenValue("dummy-token-value")
      .claim(JwtClaimNames.SUB, "some-subject-id")
      .claim(ClaimNames.CLIENT_ID, "some-client-id")
      .claim(ClaimNames.AUTH_SOURCE, authSource)
      .claim(ClaimNames.USER_ID, 100)
      .claim(ClaimNames.AUTHORITIES, listOf("auth1", "auth2"))
      .header("dummy-header", "dummy-header-value")
      .build()
  }
}
