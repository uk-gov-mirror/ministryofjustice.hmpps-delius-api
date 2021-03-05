package uk.gov.justice.digital.hmpps.deliusapi.config

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.security.DeliusSecurityService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

@ExtendWith(MockitoExtension::class)
class AuthAwareTokenConverterTest {
  @Mock private lateinit var deliusSecurity: DeliusSecurityService
  private lateinit var subject: AuthAwareTokenConverter

  @BeforeEach
  fun beforeEach() {
    subject = AuthAwareTokenConverter(deliusSecurity)
  }

  @Test
  fun `Converting subject token adds provider authorities`() {
    val user = Fake.user()
    whenever(deliusSecurity.getUser("BERNARD_BEAKS")).thenReturn(user)
    val observed = whenConverting(aToken())

    assertThat(observed)
      .hasProperty(AuthAwareAuthenticationToken::subject, "some-subject-id")
      .describedAs("should set correct authorities")
      .returns(listOf("auth1", "auth2", Authorities.PROVIDER + user.providers[0].code)) {
        it.authorities?.map { a -> a.authority }
      }
  }

  @Test
  fun `Converting client token uses database username`() {
    val user = Fake.user()
    whenever(deliusSecurity.getUser("BERNARD_BEAKS")).thenReturn(user)
    val observed = whenConverting(aToken("none", ClaimNames.DATABASE_USERNAME))

    assertThat(observed)
      .hasProperty(AuthAwareAuthenticationToken::subject, "some-subject-id")
      .describedAs("should set correct authorities")
      .returns(listOf("auth1", "auth2", Authorities.PROVIDER + user.providers[0].code)) {
        it.authorities?.map { a -> a.authority }
      }
  }

  @Test
  fun `Errors when unknown auth source`() {
    assertThrows<BadRequestException> {
      whenConverting(aToken("nomis"))
    }
  }

  @Test
  fun `Errors when client auth source and missing database username`() {
    assertThrows<BadRequestException> {
      whenConverting(aToken("none"))
    }
  }

  private fun whenConverting(jwt: Jwt) = subject.convert(jwt) as AuthAwareAuthenticationToken

  private fun aToken(authSource: String = "delius", userField: String = ClaimNames.USER_NAME): Jwt {
    return Jwt.withTokenValue("dummy-token-value")
      .claim(JwtClaimNames.SUB, "some-subject-id")
      .claim(ClaimNames.CLIENT_ID, "some-client-id")
      .claim(ClaimNames.AUTH_SOURCE, authSource)
      .claim(userField, "BERNARD_BEAKS")
      .claim(ClaimNames.AUTHORITIES, listOf("auth1", "auth2"))
      .header("dummy-header", "dummy-header-value")
      .build()
  }
}
