package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository

@ActiveProfiles("test-h2")
class DeleteContactTest @Autowired constructor(
  private val repository: ContactRepository,
) : IntegrationTestBase() {

  @Transactional
  @Test
  fun `Successfully deleting contact`() {
    val contact = havingExistingContact()
    webTestClient
      .whenDeletingContact(contact.id)
      .expectStatus().isNoContent

    val deleted = repository.findByIdOrNull(contact.id)
    assertThat(deleted).isEqualTo(null)
  }

  @Transactional
  @Test
  fun `Attempting to delete non-editable contact`() {
    webTestClient
      .whenDeletingContact(2502709905)
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage")
      .isEqualTo("Contact with id '2502709905' cannot be deleted, contact type 'ETER' is not editable")
  }

  @Transactional
  @Test
  fun `Attempting to delete unauthorised contact`() {
    userName = "chrisgooda"
    webTestClient
      .whenDeletingContact(2502719234)
      .expectStatus().isUnauthorized
  }

  @Transactional
  @Test
  fun `Attempting to delete missing contact`() {
    webTestClient
      .whenDeletingContact(100)
      .expectStatus().isNotFound
  }

  private fun WebTestClient.whenDeletingContact(id: Long) = this
    .delete().uri("/v1/contact/$id")
    .havingSimpleAuthentication()
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
}
