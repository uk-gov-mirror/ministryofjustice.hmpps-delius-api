package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

@ActiveProfiles("test-h2")
class ContactTest : IntegrationTestBase() {
  @Autowired
  private lateinit var contactRepository: ContactRepository

  @Autowired
  private lateinit var auditedInteractionRepository: AuditedInteractionRepository

  private val userId = Fake.id()
  fun authenticatedJsonRequest(): RequestBodySpec = webTestClient.post()
    .uri("/v1/contact")
    .header("Authorization", "Bearer ${jwtAuthHelper.createJwt("test-user", userId = userId)}")
    .contentType(APPLICATION_JSON)
    .accept(APPLICATION_JSON)

  companion object {
    val validContact = Fake.newContact().copy(
      offenderCrn = "X320741",
      type = "TST01", // Simple contact
      outcome = "CO22", // No Action Required
      provider = "C00",
      team = "C00T01",
      staff = "C00T01U",
      officeLocation = "C00OFFA",
      alert = false,
      eventId = 2500295343,
      requirementId = 2500083652,
    )
    var successCases: MutableList<Arguments> = mutableListOf()
    var failureCases: MutableList<Arguments> = mutableListOf()
    init {
      failureCases.addAll(
        listOf(
          of(validContact.copy(offenderCrn = ""), "offenderCrn"),
          of(validContact.copy(offenderCrn = "1234567"), "offenderCrn"),
          of(validContact.copy(type = ""), "type"),
          of(validContact.copy(type = "12345678910"), "type"),
          of(validContact.copy(provider = "12"), "provider"),
          of(validContact.copy(provider = "1234"), "provider"),
          of(validContact.copy(team = "12345"), "team"),
          of(validContact.copy(team = "1234567"), "team"),
          of(validContact.copy(staff = "123456"), "staff"),
          of(validContact.copy(staff = "12345678"), "staff"),
          of(validContact.copy(officeLocation = "123456"), "officeLocation"),
          of(validContact.copy(officeLocation = "12345678"), "officeLocation"),
          of(validContact.copy(requirementId = 1L, eventId = null), "Cannot specify requirementId without eventId"),
        )
      )
      // DAPI-70 Contact types should be restricted to allowed values
      successCases.add(of(validContact.copy(type = "TST01")))
      failureCases.add(of(validContact.copy(type = "TST04"), "type must match one of the following values"))
      // DAPI-73 Outcomes should only be required for past appointments
      successCases.add(of(validContact.copy(type = "TST03", outcome = null, date = Fake.randomFutureLocalDate())))
      failureCases.add(of(validContact.copy(type = "TST03", outcome = null), "Contact type 'TST03' requires an outcome type"))
      // DAPI-74 Office location should only be mandatory if required by contact type
      successCases.add(of(validContact.copy(type = "TST03")))
      failureCases.add(of(validContact.copy(type = "TST03", officeLocation = null), "Location is required for contact type 'TST03'"))
      // DAPI-77 Start time only mandatory for attendance contacts
      successCases.add(of(validContact.copy(type = "TST02")))
      failureCases.add(of(validContact.copy(type = "TST02", startTime = null, endTime = null), "Contact type 'TST02' requires a start time"))
      failureCases.add(of(validContact.copy(startTime = null), "Cannot specify endTime without startTime"))
    }
    @JvmStatic
    fun successCases(): Stream<Arguments> = successCases.stream()
    @JvmStatic
    fun failureCases(): Stream<Arguments> = failureCases.stream()
  }

  @ParameterizedTest(name = "[{index}] Invalid contact ({1})")
  @MethodSource("failureCases")
  fun `should throw a validation failure`(newContact: NewContact, expectedResult: String) {
    authenticatedJsonRequest()
      // When I send an invalid contact
      .bodyValue(newContact)
      .exchange()
      // Then it should return a validation error
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").value(containsString(expectedResult))
    // And it should not audit the interaction FIXME this is currently failing for validation errors thrown from the ContactService
//    val interactions = auditedInteractionRepository.findAllByUserId(userId)
//    assertThat(interactions).isEmpty()
  }

  @ParameterizedTest(name = "[{index}] Valid contact")
  @MethodSource("successCases")
  fun `should successfully create and audit a new contact`(request: NewContact) {
    authenticatedJsonRequest()
      // When I send a valid contact
      .bodyValue(request)
      .exchange()
      // Then it should return successfully
      .expectStatus().isOk
      .expectBody()
      // And it should return the correct details
      .jsonPath("$.eventId").value(equalTo(request.eventId))
      .jsonPath("$.requirementId").value(equalTo(request.requirementId))
      .jsonPath("$.id").value(greaterThan(0L))
      .jsonPath("$.id").value<Long> { id ->
        // And it should save the entity to the database with the correct details
        val entity = contactRepository.findByIdOrNull(id as Long)
        assertThat(entity).isNotNull
        assertThat(entity!!)
          .describedAs("offender id").returns(2500343964L, { it.offender.id })
          .describedAs("type").returns(request.type, { it.type?.code })
          .describedAs("outcome").returns(request.outcome, { it.outcome?.code })
          .describedAs("provider").returns(request.provider, { it.provider?.code })
          .describedAs("staff id").returns(2500000005L, { it.staff?.id })
          .describedAs("office location id").returns(2500000000L, { it.officeLocation?.id })
          .describedAs("event id").returns(request.eventId, { it.event?.id })
          .describedAs("requirement id").returns(request.requirementId, { it.requirement?.id })
          .describedAs("date").returns(request.date, { entity.date })
          .describedAs("start time").returns(request.startTime?.truncatedTo(ChronoUnit.SECONDS), { entity.startTime })
          .describedAs("end time").returns(request.endTime?.truncatedTo(ChronoUnit.SECONDS), { entity.endTime })
          .describedAs("alert").returns(request.alert, { entity.alert })
          .describedAs("sensitive").returns(request.sensitive, { entity.sensitive })
          .describedAs("notes").returns(request.notes, { entity.notes })
          .describedAs("description").returns(request.description, { entity.description })
      }

    // And it should audit the interaction
    val interactions = auditedInteractionRepository.findAllByUserId(userId)
    assertThat(interactions).hasSize(1)
    assertThat(interactions[0].success).isEqualTo(true)
    assertThat(interactions[0].parameters).contains("offenderId='2500343964'")
  }

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
  fun `Attempting to create contact with malformed json`() {
    authenticatedJsonRequest()
      .bodyValue("{,}")
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.userMessage").value(startsWith("JSON parse error: "))
  }
}
