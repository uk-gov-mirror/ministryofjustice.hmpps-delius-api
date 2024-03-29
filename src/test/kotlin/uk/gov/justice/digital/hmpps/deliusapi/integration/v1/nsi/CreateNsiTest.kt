package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.nsi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.comparingDateTimesToNearestSecond
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import uk.gov.justice.digital.hmpps.deliusapi.validation.ValidationTestCase
import uk.gov.justice.digital.hmpps.deliusapi.validation.ValidationTestCaseBuilder

@ActiveProfiles("test-h2")
class CreateNsiTest @Autowired constructor (
  private val nsiRepository: NsiRepository,
  private val contactRepository: ContactRepository,
) : IntegrationTestBase() {

  @SpyBean
  lateinit var mapper: NsiMapper

  companion object {
    @JvmStatic
    fun validTestCases() = ValidationTestCaseBuilder.fromFactory(Fake::validNewNsi)
      .setValid()
      .kitchenSink()
      .allNull(NewNsi::expectedStartDate, NewNsi::expectedEndDate)
      .allNull(NewNsi::expectedEndDate)
      .allNull(NewNsi::notes)
      .allNull(NewNsi::eventId, NewNsi::requirementId)
      .allNull(NewNsi::requirementId, NewNsi::outcome, NewNsi::endDate)
      .allNull(NewNsi::eventId, NewNsi::requirementId, NewNsi::outcome, NewNsi::endDate)
      .add("Manager team & staff not provided") { it.copy(manager = it.manager.copy(team = null, staff = null)) }
      .add("Manager staff not provided") { it.copy(manager = it.manager.copy(staff = null)) }
      .cases

    @JvmStatic
    fun invalidTestCases() = ValidationTestCaseBuilder.fromFactory(Fake::validNewNsi)
      .string(NewNsi::type) { it.empty() }
      .string(NewNsi::subType) { it.empty() }
      .string(NewNsi::offenderCrn) { it.empty() }
      .number(NewNsi::eventId) { it.zero() }
      .number(NewNsi::requirementId) { it.zero().dependent(NewNsi::eventId) }
      .date(NewNsi::referralDate) { it.tomorrow() }
      .date(NewNsi::expectedStartDate) { it.before(NewNsi::referralDate) }
      .date(NewNsi::expectedEndDate) { it.before(NewNsi::expectedStartDate).dependent(NewNsi::expectedStartDate) }
      .date(NewNsi::startDate) { it.before(NewNsi::referralDate) }
      .date(NewNsi::startDate) { it.tomorrow() }
      .date(NewNsi::endDate) { it.before(NewNsi::startDate).dependent(NewNsi::startDate) }
      .number(NewNsi::length) { it.zero() }
      .string(NewNsi::status) { it.empty() }
      .dateTime(NewNsi::statusDate) { it.beforeDate(NewNsi::referralDate).tomorrow() }
      .string(NewNsi::outcome) { it.empty().dependent(NewNsi::endDate) }
      .string(NewNsi::intendedProvider) { it.empty() }
      .add("Requirement not in the right category for the nsi") { it.copy(requirementId = 2500083653) }
      .cases
  }

  @Transactional
  @ParameterizedTest(name = "[{index}] Valid nsi {arguments}")
  @MethodSource("validTestCases")
  fun `Creating valid nsi`(case: ValidationTestCase<NewNsi>) {
    webTestClient.whenCreatingNsi(case.subject)
      .expectStatus().isCreated
      .expectBody()
      .shouldReturnCreatedNsi(case.subject)
      .shouldCreateEntityById(nsiRepository) { entity ->
        val observed = Fake.nsiMapper.toNew(Fake.nsiMapper.toDto(entity))

        assertThat(observed)
          .usingRecursiveComparison()
          .ignoringCollectionOrder()
          .comparingDateTimesToNearestSecond()
          .isEqualTo(case.subject)

        shouldCreateSystemGeneratedContact(entity.id, entity.status?.contactTypeId!!)

        assertThat(entity.statuses)
          .hasSize(1)
          .element(0)
          .hasProperty(NsiStatusHistory::notes, case.subject.notes)
          .extracting { it.status?.code }
          .isEqualTo(case.subject.status)
      }
  }

  private fun shouldCreateSystemGeneratedContact(nsiId: Long, typeId: Long) {
    val existing = contactRepository.findAllByNsiId(nsiId)
    assertThat(existing).anyMatch { it.type.id == typeId }
  }

  @ParameterizedTest(name = "[{index}] Invalid nsi ({0})")
  @MethodSource("invalidTestCases")
  fun `Attempting to create invalid nsi`(case: ValidationTestCase<NewNsi>) {
    webTestClient.whenCreatingNsi(case.subject)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError(*case.invalidPaths.toTypedArray())
  }

  @Test
  fun `Attempting to create nsi for unauthorized intended provider`() {
    userName = "automation-testzxcvbn"
    val subject = Fake.validNewNsi()
    webTestClient.whenCreatingNsi(subject)
      .expectStatus().isUnauthorized
      .expectBody().shouldReturnAccessDenied()
  }

  @Test
  fun `Attempting to create nsi for unauthorized manager provider`() {
    val subject = Fake.validNewNsi().copy(
      manager = NewNsiManager(provider = "ACI", team = null, staff = null)
    )
    webTestClient.whenCreatingNsi(subject)
      .expectStatus().isUnauthorized
      .expectBody().shouldReturnAccessDenied()
  }

  @Test
  fun `Attempting to create nsi with unauthenticated request`() {
    webTestClient.post().uri("/v1/nsi")
      .whenSendingUnauthenticatedRequest()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Attempting to create nsi with malformed json`() {
    webTestClient.post().uri("/v1/nsi")
      .whenSendingMalformedJson()
      .expectStatus().isBadRequest
      .expectBody().shouldReturnJsonParseError()
  }

  @Test
  fun `Creating a valid nsi results in increased record counts`() {
    val originalContactCount = contactRepository.count()
    val originalNsiCount = nsiRepository.count()
    val originalAuditCount = auditedInteractionRepository.count()

    webTestClient.whenCreatingNsi(Fake.validNewNsi())
      .expectStatus().is2xxSuccessful

    assertThat(contactRepository.count()).isEqualTo(originalContactCount + 4)
    assertThat(nsiRepository.count()).isEqualTo(originalNsiCount + 1)
    assertThat(auditedInteractionRepository.count()).isEqualTo(originalAuditCount + 1)
  }

  @Test
  fun `When an error occurs only the audit record is written`() {
    whenever(mapper.toDto(any<Nsi>())).thenThrow(RuntimeException("Throwing exception to trigger rollback"))

    val originalContactCount = contactRepository.count()
    val originalNsiCount = nsiRepository.count()
    val originalAuditCount = auditedInteractionRepository.count()

    webTestClient.whenCreatingNsi(Fake.validNewNsi())
      .expectStatus().is5xxServerError

    assertThat(contactRepository.count()).isEqualTo(originalContactCount)
    assertThat(nsiRepository.count()).isEqualTo(originalNsiCount)
    assertThat(auditedInteractionRepository.count()).isEqualTo(originalAuditCount + 1)
  }

  private fun WebTestClient.BodyContentSpec.shouldReturnCreatedNsi(request: NewNsi) = this
    .jsonPath("$.eventId").value(Matchers.equalTo(request.eventId))
    .jsonPath("$.requirementId").value(Matchers.equalTo(request.requirementId))
    .jsonPath("$.id").value(Matchers.greaterThan(0))
}
