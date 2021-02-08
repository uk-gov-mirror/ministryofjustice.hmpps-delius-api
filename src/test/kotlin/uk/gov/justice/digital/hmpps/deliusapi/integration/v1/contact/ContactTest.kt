package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.integration.JwtAuthHelper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class ContactTest : IntegrationTestBase() {
  @Autowired
  private lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  private lateinit var repository: ContactRepository

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
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(newContact)
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("$.id").value<Long> {
        Assertions.assertThat(it).isPositive
        Assertions.assertThat(repository.existsById(it)).describedAs("should save contact").isTrue
      }
  }
}
