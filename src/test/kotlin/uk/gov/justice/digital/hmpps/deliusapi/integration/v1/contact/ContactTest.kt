package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.integration.JwtAuthHelper
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class ContactTest : IntegrationTestBase() {
  @Autowired
  private lateinit var jwtAuthHelper: JwtAuthHelper

  @Test
  fun `Attempting to create contact without authentication`() {
    val newContact = Fake.newContact()
    webTestClient.post()
      .uri("/v1/contact")
      .bodyValue(newContact)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Attempting to create invalid contact`() {
    val token = jwtAuthHelper.createJwt("bob")
    val newContact = Fake.newContact(object { val offenderId = 0 })
    webTestClient.post()
      .uri("/v1/contact")
      .header("Authorization", "Bearer $token")
      .bodyValue(newContact)
      .exchange()
      .expectStatus()
      .isBadRequest
  }

  @Test
  fun `Creating contact`() {
    val token = jwtAuthHelper.createJwt("bob")
    val newContact = Fake.newContact(object { val offenderId = 11 })
    webTestClient.post()
      .uri("/v1/contact")
      .header("Authorization", "Bearer $token")
      .bodyValue(newContact)
      .exchange()
      .expectStatus()
      .isOk
  }
}
