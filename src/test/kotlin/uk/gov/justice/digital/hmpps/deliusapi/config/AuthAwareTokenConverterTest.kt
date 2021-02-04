package uk.gov.justice.digital.hmpps.deliusapi.config

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames

class AuthAwareTokenConverterTest {
  private val subject = AuthAwareTokenConverter()

  @Test
  fun `Converting subject token`() {
    val observed = whenConverting(aToken())
    shouldConvertToAuthAwareAuthenticationToken(observed)
  }

  private fun whenConverting(jwt: Jwt): AuthAwareAuthenticationToken? {
    return subject.convert(jwt) as? AuthAwareAuthenticationToken
  }

  private fun aToken(): Jwt {
    return Jwt.withTokenValue("dummy-token-value")
      .claim(JwtClaimNames.SUB,"some-subject-id")
      .claim(ClaimNames.CLIENT_ID, "some-client-id")
      .claim(ClaimNames.AUTHORITIES, listOf("auth1", "auth2"))
      .header("dummy-header", "dummy-header-value")
      .build()
  }

  private fun shouldConvertToAuthAwareAuthenticationToken(observed: AuthAwareAuthenticationToken?) {
    Assertions.assertThat(observed).isNotNull.isInstanceOf(AuthAwareAuthenticationToken::class.java)
    Assertions.assertThat(observed?.authorities?.map { it.authority }).containsOnly("auth1", "auth2")
    Assertions.assertThat(observed?.subject).isEqualTo("some-subject-id")
  }
}
