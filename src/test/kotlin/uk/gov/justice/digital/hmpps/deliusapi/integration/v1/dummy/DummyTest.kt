package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.dummy

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.integration.JwtAuthHelper

class DummyTest : IntegrationTestBase() {
  @Autowired
  private lateinit var jwtAuthHelper: JwtAuthHelper

  @Test
  fun `Attempting to get dummy data without authentication`() {
    webTestClient.get()
      .uri("/v1/dummy")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Getting dummy data`() {
    val token = jwtAuthHelper.createJwt("bob")
    webTestClient.get()
      .uri("/v1/dummy")
      .header("Authorization", "Bearer $token")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("message").isEqualTo("hello world")
  }
}
