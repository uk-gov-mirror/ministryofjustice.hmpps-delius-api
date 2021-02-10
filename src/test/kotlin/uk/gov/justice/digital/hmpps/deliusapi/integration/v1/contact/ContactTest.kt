package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ActiveProfiles("test-h2")
class ContactTest : IntegrationTestBase() {
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
    val newContact = Fake.newContact(object { val offenderCrn = "bacon" })
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
    val newContact = Fake.newContact(
      object {
        val offenderCrn = "X320811"
        val contactType = "C130"
        val contactOutcome = "RD01"
        val provider = "ESX"
        val team = "A00N07"
        val staff = "TESUATU"
        val officeLocation = "DRSTURN"
      }
    )
    var id = 0L
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
      .jsonPath("$.id").value<Long> { id = it }

    Assertions.assertThat(id).describedAs("should return contact id").isPositive
    val entity = repository.findByIdOrNull(id)
    Assertions.assertThat(entity).describedAs("should save contact").isNotNull
      .extracting { it?.offender?.id }
      .describedAs("should save expected offender")
      .isEqualTo(2600343964L)
  }
}
