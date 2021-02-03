package uk.gov.justice.digital.hmpps.deliusapi.config

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames

class AuthAwareTokenConverterTest {
  private val subject = AuthAwareTokenConverter()

  @Test
  fun `converting client credentials token`() {
    val observed = whenConverting(aToken(true))

    shouldConvertToAuthAwareAuthenticationToken(observed)
    Assertions.assertThat(observed?.clientOnly).isTrue
    Assertions.assertThat(observed?.subject).isEqualTo("some-client-id")
  }

  @Test
  fun `converting subject token`() {
    val observed = whenConverting(aToken())

    shouldConvertToAuthAwareAuthenticationToken(observed)
    Assertions.assertThat(observed?.clientOnly).isFalse
    Assertions.assertThat(observed?.subject).isEqualTo("some-subject-id")
  }

  private fun whenConverting(jwt: Jwt): AuthAwareAuthenticationToken? {
    return subject.convert(jwt) as? AuthAwareAuthenticationToken
  }

  private fun aToken(clientCredentials: Boolean = false): Jwt {
    return Jwt.withTokenValue("dummy-token-value")
      .claim(JwtClaimNames.SUB, if (clientCredentials) "some-client-id" else "some-subject-id")
      .claim(ClaimNames.CLIENT_ID, "some-client-id")
      .claim(ClaimNames.DATABASE_USERNAME, "some-database-username")
      .claim(ClaimNames.AUTHORITIES, listOf("auth1", "auth2"))
      .header("dummy-header", "dummy-header-value")
      .build()
  }

  private fun shouldConvertToAuthAwareAuthenticationToken(observed: AuthAwareAuthenticationToken?) {
    Assertions.assertThat(observed).isNotNull
    Assertions.assertThat(observed).isInstanceOf(AuthAwareAuthenticationToken::class.java)

    observed !!
    Assertions.assertThat(observed.databaseUsername).isEqualTo("some-database-username")
    Assertions.assertThat(observed.authorities.map { it.authority }).containsOnly("auth1", "auth2")
  }
}
