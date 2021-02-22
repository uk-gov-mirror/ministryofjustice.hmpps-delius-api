package uk.gov.justice.digital.hmpps.deliusapi.integration

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class JwtAuthHelper {
  private val keyPair: KeyPair

  init {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    keyPair = gen.generateKeyPair()
  }

  @Bean
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun createJwt(
    subject: String?,
    userId: Long? = Fake.randomPositiveId(),
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expired: Boolean = false,
    jwtId: String = UUID.randomUUID().toString(),
    deliusUser: Boolean = true,
  ): String {
    val claims = mutableMapOf<String, Any?>(
      "user_name" to subject,
      "client_id" to "elite2apiclient",
      "user_id" to userId,
      "auth_source" to if (deliusUser) "delius" else "hmpps"
    )
    roles?.let { claims["authorities"] = roles }
    scope?.let { claims["scope"] = scope }

    val expiration = if (expired)
      Fake.faker.date().past(12, 1, TimeUnit.HOURS)
    else Fake.faker.date().future(12, 1, TimeUnit.HOURS)

    return Jwts.builder()
      .setId(jwtId)
      .setSubject(subject)
      .addClaims(claims)
      .setExpiration(expiration)
      .signWith(SignatureAlgorithm.RS256, keyPair.private)
      .compact()
  }
}
