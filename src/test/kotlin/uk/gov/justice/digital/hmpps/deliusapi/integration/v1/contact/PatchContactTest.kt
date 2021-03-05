package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.ContactService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KProperty1

data class Operation(val op: String, val path: String, val value: Any? = null)

class UpdateCase(
  vararg val operations: Operation,
  val expected: (contact: Contact) -> Map<KProperty1<ContactDto, *>, Any?>,
)

@ActiveProfiles("test-h2")
class PatchContactTest @Autowired constructor(
  private val contactRepository: ContactRepository
) : IntegrationTestBase() {

  companion object {
    @JvmStatic
    fun validCases() = listOf(
      UpdateCase(
        Operation("replace", "/outcome", "UBHV"),
        Operation("replace", "/provider", "N02"),
        Operation("replace", "/team", "N02T01"),
        Operation("replace", "/staff", "N02P002"),
        Operation("replace", "/officeLocation", "DTVBIS1"),
        Operation("replace", "/date", "2021-03-01"),
        Operation("replace", "/startTime", "12:00:00"),
        Operation("replace", "/endTime", "13:00:00"),
        Operation("replace", "/alert", "true"),
        Operation("replace", "/sensitive", "true"),
        Operation("replace", "/notes", "updated notes"),
        Operation("replace", "/description", "updated description"),
      ) {
        mapOf(
          ContactDto::outcome to "UBHV",
          ContactDto::provider to "N02",
          ContactDto::team to "N02T01",
          ContactDto::staff to "N02P002",
          ContactDto::officeLocation to "DTVBIS1",
          ContactDto::date to LocalDate.of(2021, 3, 1),
          ContactDto::startTime to LocalTime.of(12, 0),
          ContactDto::endTime to LocalTime.of(13, 0),
          ContactDto::alert to true,
          ContactDto::sensitive to true,
          // notes are not actually replaced, they are appended
          ContactDto::notes to it.notes + ContactService.NOTES_SEPARATOR + "updated notes",
          ContactDto::description to "updated description",
        )
      },
      UpdateCase(Operation("remove", "/endTime")) { mapOf(ContactDto::endTime to null) },
      UpdateCase(Operation("remove", "/description")) { mapOf(ContactDto::description to null) },
      // notes are immutable
      UpdateCase(Operation("remove", "/notes")) { mapOf(ContactDto::notes to it.notes) },
    )

    @JvmStatic
    fun invalidCases() = listOf(
      of("no such path", Operation("replace", "/bacon", "eggs")),
      of("requires an outcome type", Operation("remove", "/outcome")),
      of("provider is required", Operation("remove", "/provider")),
      of("must be a valid provider code", Operation("replace", "/provider", "AAAA")),
      of("must be a valid team code", Operation("replace", "/team", "AAAAAAA")),
      of("must be a valid staff code", Operation("replace", "/staff", "AAAAAAAA")),
      of("must be a valid office location code", Operation("replace", "/officeLocation", "AAAAAAAA")),
      of("date is not a valid LocalDate", Operation("replace", "/date", "A")),
      of("startTime is not a valid LocalTime", Operation("replace", "/startTime", "A")),
      of("endTime is not a valid LocalTime", Operation("replace", "/endTime", "A")),
      of("alert is not a valid boolean", Operation("replace", "/alert", "A")),
      of("sensitive is not a valid boolean", Operation("replace", "/sensitive", "A")),
    )

    @JvmStatic
    fun unauthorizedCases() = listOf(
      Operation("replace", "/provider", "AAA"),
    )
  }

  @ParameterizedTest(name = "[{index}] Valid patch contact {arguments}")
  @MethodSource("validCases")
  fun `Successfully patching existing contact`(case: UpdateCase) {
    val contact = havingExistingContact()
    val expected = case.expected(contact)

    webTestClient
      .whenPatchingContact(contact.id, *case.operations)
      .expectStatus().isOk

    val updated = contactRepository.findByIdOrNull(contact.id)
    val observed = ContactMapper.INSTANCE.toDto(updated!!)

    expected.toList().fold(assertThat(observed)) { it, (k, v) -> it.hasProperty(k, v) }
  }

  @ParameterizedTest(name = "[{index}] Invalid patch contact {0}")
  @MethodSource("invalidCases")
  fun `Attempting to patch existing contact with invalid patch`(name: String, operation: Operation) {
    val (id) = havingExistingContact()
    webTestClient
      .whenPatchingContact(id, operation)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError(name)
  }

  @ParameterizedTest(name = "[{index}] Unauthorized patch contact {0}")
  @MethodSource("unauthorizedCases")
  fun `Attempting to patch existing contact with unauthorized patch`(operation: Operation) {
    val (id) = havingExistingContact()
    webTestClient
      .whenPatchingContact(id, operation)
      .expectStatus().isUnauthorized
  }

  fun havingExistingContact(): Contact {
    var id = 0L
    val request = Fake.validNewContact()
    webTestClient.whenCreatingContact(request)
      .expectStatus().isCreated
      .expectBody()
      .jsonPath("$.id").value<Long> { id = it }
    assertThat(id).describedAs("should create contact & return id").isPositive
    return contactRepository.findByIdOrNull(id)!!
  }

  private fun WebTestClient.whenPatchingContact(id: Long, vararg operations: Operation): WebTestClient.ResponseSpec {
    return patch()
      .uri("/v1/contact/$id")
      .havingAuthentication()
      .contentType(MediaType.parseMediaType("application/json-patch+json"))
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(operations)
      .exchange()
  }
}
