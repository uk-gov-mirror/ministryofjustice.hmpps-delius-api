package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.nsi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.integration.Operation
import uk.gov.justice.digital.hmpps.deliusapi.integration.UpdateCase
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

@ActiveProfiles("test-h2")
class PatchNsiTest @Autowired constructor(
  private val nsiRepository: NsiRepository,
  private val mapper: NsiMapper,
) : IntegrationTestBase() {

  companion object {
    @JvmStatic
    fun validCases() = listOf(
      UpdateCase<NsiDto>(
        Operation("replace", "/expectedStartDate", LocalDate.of(2017, 6, 2)),
        Operation("replace", "/expectedEndDate", LocalDate.of(2017, 12, 2)),
        Operation("replace", "/startDate", LocalDate.of(2017, 6, 2)),
        Operation("replace", "/endDate", LocalDate.of(2017, 12, 2)),
        Operation("replace", "/length", 2),
        Operation("replace", "/status", "REFER"),
        Operation("replace", "/statusDate", LocalDateTime.now().minusDays(1)),
        Operation("replace", "/notes", "new notes"),
        Operation("replace", "/manager/team", "C00T03"),
        Operation("replace", "/manager/staff", "C00P032"),
        Operation("replace", "/manager/transferDate", LocalDate.of(2017, 7, 1)),
        Operation("replace", "/manager/transferReason", "TIN"),
      ) {
        mapOf(
          NsiDto::expectedStartDate to LocalDate.of(2017, 6, 2),
          NsiDto::expectedEndDate to LocalDate.of(2017, 12, 2),
          NsiDto::startDate to LocalDate.of(2017, 6, 2),
          NsiDto::endDate to LocalDate.of(2017, 12, 2),
          NsiDto::length to 2L,
          NsiDto::status to "REFER",
          NsiDto::notes to it.notes + "\n" + "new notes",
          NsiDto::manager to NsiManagerDto(
            id = it.manager.id + 1,
            provider = it.manager.provider,
            team = "C00T03",
            staff = "C00P032",
          )
        )
      },
      UpdateCase(Operation("replace", "/outcome", "NCMP")) { mapOf(NsiDto::outcome to "NCMP") },
      UpdateCase(
        Operation("remove", "/expectedStartDate"),
        Operation("remove", "/expectedEndDate"),
        Operation("remove", "/startDate"),
        Operation("remove", "/endDate"),
        Operation("remove", "/outcome"),
      ) {
        mapOf(
          NsiDto::expectedStartDate to null,
          NsiDto::expectedEndDate to null,
          NsiDto::startDate to null,
          NsiDto::endDate to null,
          NsiDto::outcome to null,
        )
      },
    )

    @JvmStatic
    fun invalidCases() = listOf(
      Arguments.of("no such path", Operation("replace", "/bacon", "eggs")),
      Arguments.of("", Operation("replace", "/startDate", "bacon")),
      Arguments.of(
        "Cannot update the referral date",
        Operation("replace", "/referralDate", LocalDate.of(2017, 6, 2))
      ),
      Arguments.of(
        "expectedStartDate must be after or equal to referralDate",
        Operation("replace", "/expectedStartDate", LocalDate.of(2017, 5, 31))
      ),
      Arguments.of(
        "expectedEndDate must be after or equal to expectedStartDate",
        Operation("replace", "/expectedEndDate", LocalDate.of(2017, 5, 31))
      ),
      Arguments.of(
        "startDate must be after or equal to referralDate",
        Operation("replace", "/startDate", LocalDate.of(2017, 5, 31))
      ),
      Arguments.of(
        "endDate must be after or equal to startDate",
        Operation("replace", "/endDate", LocalDate.of(2017, 5, 31))
      ),
      Arguments.of(
        "requires a length",
        Operation("remove", "/length")
      ),
      Arguments.of(
        "requires a length between 1 & 2",
        Operation("replace", "/length", 3)
      ),
      Arguments.of(
        "is not a valid status",
        Operation("replace", "/status", "unknown-status")
      ),
      Arguments.of(
        "statusDate: must be a date in the past or in the present",
        Operation("replace", "/statusDate", LocalDateTime.now().plusDays(1))
      ),
      Arguments.of(
        "is not a valid outcome type",
        Operation("replace", "/outcome", "unknown-outcome")
      ),
      Arguments.of(
        "endDate: cannot be provided without also providing outcome",
        Operation("remove", "/outcome")
      ),
      Arguments.of(
        "Team with code 'ABC123' does not exist for provider 'C00'",
        Operation("replace", "/manager/team", "ABC123")
      ),
      Arguments.of(
        "must be a valid team code",
        Operation("replace", "/manager/team", Fake.faker.lorem().characters(7))
      ),
      Arguments.of(
        "Staff with officer code 'ABC123' does not exist in team 'C00T02'",
        Operation("replace", "/manager/staff", "ABC123")
      ),
      Arguments.of(
        "must be a valid staff code",
        Operation("replace", "/manager/staff", Fake.faker.lorem().characters(8))
      ),
      Arguments.of(
        "Transfer reason is required to update an NSI manager",
        Operation("replace", "/manager/staff", "C00P029")
      ),
    )
  }

  @Transactional
  @ParameterizedTest(name = "[{index}] Valid patch nsi {arguments}")
  @MethodSource("validCases")
  fun `Successfully patching nsi`(case: UpdateCase<NsiDto>) {
    val nsi = havingExistingNsi()
    val expected = case.expected(nsi)

    webTestClient
      .whenPatchingNsi(nsi.id, *case.operations)
      .expectStatus().isOk
      .shouldReturnExpectedDto(nsi.id, expected)

    shouldUpdateNsi(nsi.id, expected)
  }

  @ParameterizedTest(name = "[{index}] Invalid patch nsi {0}")
  @MethodSource("invalidCases")
  fun `Attempting to patch existing nsi with invalid patch`(name: String, operation: Operation) {
    val (id) = havingExistingNsi()
    webTestClient
      .whenPatchingNsi(id, operation)
      .expectStatus().isBadRequest
      .expectBody().shouldReturnValidationError(name)
  }

  @Test
  @Transactional
  fun `Successfully patching nsi status`() {
    val nsi = havingExistingNsi()

    val statusDate = LocalDateTime.now().minusDays(1).withNano(0)
    webTestClient
      .whenPatchingNsi(
        nsi.id,
        Operation("replace", "/status", "REFER"),
        Operation("replace", "/statusDate", statusDate)
      )
      .expectStatus().isOk

    val updated = nsiRepository.findByIdOrNull(nsi.id)
      ?: throw RuntimeException("NSI with id '${nsi.id}' does not exist")

    assertThat(updated.status!!.code).isEqualTo("REFER")
    assertThat(updated.statusDate).isEqualTo(statusDate)

    val description = updated.statuses.joinToString(", ") { s -> "[${s.id}] ${s.date} ${s.status!!.code}" }
    assertThat(updated.statuses)
      .hasSize(2)
      .describedAs("should have original status: $description").anyMatch { it.status!!.code == nsi.status }
      .describedAs("should have new status: $description").anyMatch { it.status!!.code == "REFER" }
  }

  private fun havingExistingNsi(request: NewNsi = Fake.validNewNsi()): NsiDto {
    return webTestClient.whenCreatingNsi(request)
      .expectStatus().isCreated
      .expectBody(NsiDto::class.java)
      .returnResult()
      .responseBody
  }

  private fun WebTestClient.ResponseSpec.shouldReturnExpectedDto(id: Long, expected: Map<KProperty1<NsiDto, *>, Any?>) {
    val observed = expectBody(NsiDto::class.java).returnResult().responseBody
    assertThat(observed.id).isEqualTo(id)
    expected.toList().fold(assertThat(observed)) { it, (k, v) -> it.hasProperty(k, v) }
  }

  private fun WebTestClient.whenPatchingNsi(id: Long, vararg operations: Operation) =
    whenPatching("nsi", id, *operations)

  private fun shouldUpdateNsi(id: Long, expected: Map<KProperty1<NsiDto, *>, Any?>) {
    val updated = nsiRepository.findByIdOrNull(id)
    val observed = mapper.toDto(updated!!)
    expected.toList().fold(assertThat(observed)) { it, (k, v) -> it.hasProperty(k, v) }
  }
}
