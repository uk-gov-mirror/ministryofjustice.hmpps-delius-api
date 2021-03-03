package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.comparingDateTimesToNearestSecond
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.util.stream.Stream

@ActiveProfiles("test-h2")
class CreateContactTest : IntegrationTestBase() {
  @Autowired
  private lateinit var contactRepository: ContactRepository

  companion object {
    private val valid = Fake.newContact().copy(
      offenderCrn = "X320741",
      type = "TST01", // Simple contact
      outcome = "CO22", // No Action Required
      nsiId = null,
      provider = "C00",
      team = "C00T01",
      staff = "C00T01U",
      officeLocation = "C00OFFA",
      alert = false,
      eventId = 2500295343,
      requirementId = 2500083652,
    )
    private val successCases: MutableList<Arguments> = mutableListOf()
    private val failureCases: MutableList<Arguments> = mutableListOf()
    init {
      failureCases.addAll(
        listOf(
          of(valid.copy(offenderCrn = ""), "offenderCrn"),
          of(valid.copy(offenderCrn = "1234567"), "offenderCrn"),
          of(valid.copy(type = ""), "type"),
          of(valid.copy(type = "12345678910"), "type"),
          of(valid.copy(provider = "12"), "provider"),
          of(valid.copy(provider = "1234"), "provider"),
          of(valid.copy(team = "12345"), "team"),
          of(valid.copy(team = "1234567"), "team"),
          of(valid.copy(staff = "123456"), "staff"),
          of(valid.copy(staff = "12345678"), "staff"),
          of(valid.copy(officeLocation = "123456"), "officeLocation"),
          of(valid.copy(officeLocation = "12345678"), "officeLocation"),
          of(valid.copy(requirementId = 1L, eventId = null), "Cannot specify requirementId without eventId"),
          of(valid.copy(requirementId = 2500083652, nsiId = 2500018597), "Only one of nsiId, requirementId can have a value"),
        )
      )
      // DAPI-70 Contact types should be restricted to allowed values
      successCases.add(of(valid.copy(type = "TST01")))
      failureCases.add(of(valid.copy(type = "TST04"), "type must match one of the following values"))
      // DAPI-73 Outcomes should only be required for past appointments
      successCases.add(of(valid.copy(type = "TST03", outcome = null, date = Fake.randomFutureLocalDate())))
      failureCases.add(of(valid.copy(type = "TST03", outcome = null), "Contact type 'TST03' requires an outcome type"))
      // DAPI-74 Office location should only be mandatory if required by contact type
      successCases.add(of(valid.copy(type = "TST03")))
      failureCases.add(of(valid.copy(type = "TST03", officeLocation = null), "Location is required for contact type 'TST03'"))
      // DAPI-77 Start time only mandatory for attendance contacts
      successCases.add(of(valid.copy(type = "TST02")))
      failureCases.add(of(valid.copy(type = "TST02", startTime = null, endTime = null), "Contact type 'TST02' requires a start time"))
      failureCases.add(of(valid.copy(startTime = null), "Cannot specify endTime without startTime"))
      // Non-selectable contact types with SPG Override set are allowed
      successCases.add(of(valid.copy(type = "TST05")))
      // Non-selectable contact types without SPG Override set are not allowed
      failureCases.add(of(valid.copy(type = "SMLI001"), "Contact type with code 'SMLI001' does not exist"))
      // NSI and event supplied, requirement left blank
      successCases.add(of(valid.copy(nsiId = 2500018597, eventId = 2500295343, requirementId = null)))
    }
    @JvmStatic
    fun successCases(): Stream<Arguments> = successCases.stream()
    @JvmStatic
    fun failureCases(): Stream<Arguments> = failureCases.stream()
  }

  @ParameterizedTest(name = "[{index}] Invalid contact ({1})")
  @MethodSource("failureCases")
  fun `should throw a validation failure`(request: NewContact, expectedResult: String) {
    webTestClient.whenCreatingContact(request)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError(expectedResult)

    shouldNotAudit(AuditableInteraction.ADD_CONTACT)
  }

  @ParameterizedTest(name = "[{index}] Valid contact")
  @MethodSource("successCases")
  fun `should successfully create and audit a new contact`(request: NewContact) {
    webTestClient.whenCreatingContact(request)
      // Then it should return successfully
      .expectStatus().isCreated
      .expectBody()
      // And it should return the correct details
      .shouldReturnCreatedContact(request)
      // And it should save the entity to the database with the correct details
      .shouldSaveContact(request)

    shouldAudit(AuditableInteraction.ADD_CONTACT, mapOf("offenderId" to 2500343964L))
  }

  @Test
  fun `Attempting to create contact without authentication`() {
    webTestClient.post().uri("/v1/contact")
      .whenSendingUnauthenticatedRequest()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Attempting to create contact with malformed json`() {
    webTestClient.post().uri("/v1/contact")
      .whenSendingMalformedJson()
      .expectStatus().isBadRequest
      .expectBody().shouldReturnJsonParseError()
  }

  private fun WebTestClient.whenCreatingContact(request: NewContact) = this
    .post().uri("/v1/contact")
    .havingAuthentication()
    .contentType(APPLICATION_JSON)
    .accept(APPLICATION_JSON)
    .bodyValue(request)
    .exchange()

  private fun WebTestClient.BodyContentSpec.shouldReturnCreatedContact(request: NewContact): WebTestClient.BodyContentSpec {
    jsonPath("$.eventId").value(equalTo(request.eventId))
    jsonPath("$.id").value(greaterThan(0L))

    if (request.requirementId != null) {
      jsonPath("$.requirementId").value(equalTo(request.requirementId))
    }

    if (request.nsiId != null) {
      jsonPath("$.nsiId").value(equalTo(request.nsiId))
    }
    return this
  }

  private fun WebTestClient.BodyContentSpec.shouldSaveContact(request: NewContact) = this
    .shouldCreateEntityById(contactRepository) { entity ->

      val observed = Fake.contactMapper.toNew(Fake.contactMapper.toDto(entity))
      assertThat(observed)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .comparingDateTimesToNearestSecond()
        .isEqualTo(request)

      assertThat(entity)
        .hasProperty(Contact::partitionAreaId, 0L)
        .hasProperty(Contact::staffEmployeeId, 1L)
        .hasProperty(Contact::teamProviderId, 1L)
    }
}
