package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.util.stream.Stream

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

  companion object {
    @JvmStatic
    fun invalidContactTestCases(): Stream<Arguments> =
      Stream.of(
        Arguments.of(Fake.newContact(object { val offenderCrn = "" }), "offenderCrn"),
        Arguments.of(Fake.newContact(object { val offenderCrn = "1234567" }), "offenderCrn"),
        Arguments.of(Fake.newContact(object { val contactType = "" }), "contactType"),
        Arguments.of(Fake.newContact(object { val contactType = "12345678910" }), "contactType"),
        Arguments.of(Fake.newContact(object { val provider = "12" }), "provider"),
        Arguments.of(Fake.newContact(object { val provider = "1234" }), "provider"),
        Arguments.of(Fake.newContact(object { val team = "12345" }), "team"),
        Arguments.of(Fake.newContact(object { val team = "1234567" }), "team"),
        Arguments.of(Fake.newContact(object { val staff = "123456" }), "staff"),
        Arguments.of(Fake.newContact(object { val staff = "12345678" }), "staff"),
        Arguments.of(Fake.newContact(object { val officeLocation = "123456" }), "officeLocation"),
        Arguments.of(Fake.newContact(object { val officeLocation = "12345678" }), "officeLocation"),
      )
  }

  @ParameterizedTest
  @MethodSource("invalidContactTestCases")
  fun `Attempting to create invalid contact`(newContact: NewContact, name: String) {
    val token = jwtAuthHelper.createJwt("bob")
    var userMessage = ""
    webTestClient.post()
      .uri("/v1/contact")
      .header("Authorization", "Bearer $token")
      .bodyValue(newContact)
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").value<String> { userMessage = it }

    Assertions.assertThat(userMessage).startsWith("Validation failure: ").contains(name)
  }

  @Test
  fun `Attempting to create contact with malformed json`() {
    val token = jwtAuthHelper.createJwt("bob")
    var userMessage = ""
    webTestClient.post()
      .uri("/v1/contact")
      .header("Authorization", "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue("{,}")
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").value<String> { userMessage = it }

    Assertions.assertThat(userMessage).startsWith("JSON parse error: ")
  }

  @Test
  fun `Creating contact`() {
    val token = jwtAuthHelper.createJwt("bob")
    val newContact = Fake.newContact(
      object {
        val offenderCrn = "X320811"
        val contactType = "COUP" // Unplanned Contact from Offender
        val contactOutcome = "CO22" // No Action Required
        val provider = "C00"
        val team = "C00T01"
        val staff = "C00T01U"
        val officeLocation = "C00OFFA"
        val alert = false
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
