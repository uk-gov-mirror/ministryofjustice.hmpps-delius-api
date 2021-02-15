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
        Arguments.of(Fake.newContact().copy(offenderCrn = ""), "offenderCrn"),
        Arguments.of(Fake.newContact().copy(offenderCrn = "1234567"), "offenderCrn"),
        Arguments.of(Fake.newContact().copy(contactType = ""), "contactType"),
        Arguments.of(Fake.newContact().copy(contactType = "12345678910"), "contactType"),
        Arguments.of(Fake.newContact().copy(provider = "12"), "provider"),
        Arguments.of(Fake.newContact().copy(provider = "1234"), "provider"),
        Arguments.of(Fake.newContact().copy(team = "12345"), "team"),
        Arguments.of(Fake.newContact().copy(team = "1234567"), "team"),
        Arguments.of(Fake.newContact().copy(staff = "123456"), "staff"),
        Arguments.of(Fake.newContact().copy(staff = "12345678"), "staff"),
        Arguments.of(Fake.newContact().copy(officeLocation = "123456"), "officeLocation"),
        Arguments.of(Fake.newContact().copy(officeLocation = "12345678"), "officeLocation"),
        Arguments.of(Fake.newContact().copy(requirementId = 1L, eventId = null), "eventProvidedWithRequirement"),
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
    val newContact = Fake.newContact().copy(
      offenderCrn = "X320741",
      contactType = "COUP", // Unplanned Contact from Offender
      contactOutcome = "CO22", // No Action Required
      provider = "C00",
      team = "C00T01",
      staff = "C00T01U",
      officeLocation = "C00OFFA",
      alert = false,
      eventId = 2500295343L,
      requirementId = 2500083652,
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

    Assertions.assertThat(entity?.offender?.id)
      .describedAs("should save expected offender")
      .isEqualTo(2500343964L)

    Assertions.assertThat(entity?.contactType?.id)
      .describedAs("should save expected type")
      .isEqualTo(327L)

    Assertions.assertThat(entity?.contactOutcomeType?.id)
      .describedAs("should save expected outcome type")
      .isEqualTo(94L)

    Assertions.assertThat(entity?.provider?.id)
      .describedAs("should save expected provider")
      .isEqualTo(2500000002L)

    Assertions.assertThat(entity?.team?.id)
      .describedAs("should save expected team")
      .isEqualTo(2500000005L)

    Assertions.assertThat(entity?.staff?.id)
      .describedAs("should save expected staff")
      .isEqualTo(2500000005L)

    Assertions.assertThat(entity?.officeLocation?.id)
      .describedAs("should save expected office location")
      .isEqualTo(2500000000L)

    Assertions.assertThat(entity?.contactDate).isEqualTo(newContact.contactDate)
    Assertions.assertThat(entity?.contactStartTime).isEqualToIgnoringNanos(newContact.contactStartTime)
    Assertions.assertThat(entity?.contactEndTime).isEqualToIgnoringNanos(newContact.contactEndTime)
    Assertions.assertThat(entity?.alert).isFalse
    Assertions.assertThat(entity?.sensitive).isEqualTo(newContact.sensitive)
    Assertions.assertThat(entity?.notes).isEqualTo(newContact.notes)
    Assertions.assertThat(entity?.description).isEqualTo(newContact.description)

    Assertions.assertThat(entity?.event?.id)
      .describedAs("should save expected event")
      .isNotNull
      .isEqualTo(newContact.eventId)

    Assertions.assertThat(entity?.requirement?.id)
      .describedAs("should save expected requirement")
      .isNotNull
      .isEqualTo(newContact.requirementId)
  }
}
