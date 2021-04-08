package uk.gov.justice.digital.hmpps.deliusapi.v1.nsi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.NsiTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.staffCodeOrUnallocated
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.teamCodeOrUnallocated
import uk.gov.justice.digital.hmpps.deliusapi.util.extractingObject
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalDateTime

class CreateNsiV1Test @Autowired constructor(
  private val repository: NsiRepository,
) : EndToEndTest() {
  private lateinit var request: NewNsi
  private lateinit var response: NsiDto
  private lateinit var created: Nsi

  @Test
  fun `Creating active nsi`() {
    request = configuration.newNsi(NsiTestsConfiguration::active)
    whenCreatingNsi()
    shouldReturnExpectedNsi()
    shouldSaveNsi()
  }

  @Test
  fun `Creating terminated nsi`() {
    request = configuration.newNsi(NsiTestsConfiguration::terminated).copy(
      expectedStartDate = LocalDate.of(2021, 1, 4),
      expectedEndDate = LocalDate.of(2021, 2, 1),
      referralDate = LocalDate.of(2021, 1, 4),
      startDate = LocalDate.of(2021, 1, 4),
      statusDate = LocalDateTime.of(2021, 1, 4, 12, 0, 0),
      endDate = LocalDate.of(2021, 2, 1),
    )
    whenCreatingNsi()
    shouldReturnExpectedNsi()
    shouldSaveNsi()
  }

  private fun whenCreatingNsi() {
    response = nsiV1.safely { it.createNsi(request) }
  }

  private fun shouldReturnExpectedNsi() {
    assertThat(response)
      .hasFieldOrProperty("id")
      .hasProperty(NsiDto::type, request.type)
      .hasProperty(NsiDto::subType, request.subType)
      .hasProperty(NsiDto::offenderCrn, request.offenderCrn)
      .hasProperty(NsiDto::eventId, request.eventId)
      .hasProperty(NsiDto::requirementId, request.requirementId)
      .hasProperty(NsiDto::referralDate, request.referralDate)
      .hasProperty(NsiDto::expectedStartDate, request.expectedStartDate)
      .hasProperty(NsiDto::expectedEndDate, request.expectedEndDate)
      .hasProperty(NsiDto::startDate, request.startDate)
      .hasProperty(NsiDto::endDate, request.endDate)
      .hasProperty(NsiDto::length, request.length)
      .hasProperty(NsiDto::status, request.status)
      .hasProperty(NsiDto::statusDate, request.statusDate)
      .hasProperty(NsiDto::outcome, request.outcome)
      .hasProperty(NsiDto::notes, request.notes)
      .hasProperty(NsiDto::intendedProvider, request.intendedProvider)
      .extractingObject { it.manager }
      .hasProperty(NsiManagerDto::provider, request.manager?.provider)
      .hasProperty(NsiManagerDto::team, teamCodeOrUnallocated(request.manager?.team!!))
      .hasProperty(NsiManagerDto::staff, staffCodeOrUnallocated(request.manager?.staff!!))
  }

  private fun shouldSaveNsi() {
    if (!databaseAssertEnabled()) {
      return
    }

    created = repository.findByIdOrNull(response.id)
      ?: throw RuntimeException("NSI with id = '${response.id}' does not exist in the database")

    TODO("implement assertions against saved nsi")
  }
}
