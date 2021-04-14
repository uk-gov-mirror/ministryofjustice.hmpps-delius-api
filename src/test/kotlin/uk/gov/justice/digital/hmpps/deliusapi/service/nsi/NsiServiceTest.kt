package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.util.Optional

@ExtendWith(MockitoExtension::class)
abstract class NsiServiceTest {
  @Mock protected lateinit var nsiRepository: NsiRepository
  @Mock protected lateinit var offenderRepository: OffenderRepository
  @Mock protected lateinit var nsiTypeRepository: NsiTypeRepository
  @Mock protected lateinit var mapper: NsiMapper
  @Spy protected lateinit var features: FeatureFlags
  @Mock protected lateinit var validation: NsiValidationService
  @Mock protected lateinit var nsiManagerService: NsiManagerService
  @Mock protected lateinit var nsiSystemContactService: NsiSystemContactService
  @Captor protected lateinit var captor: ArgumentCaptor<Nsi>
  @InjectMocks protected lateinit var subject: NsiService

  protected lateinit var mapped: NsiDto
  protected lateinit var observed: NsiDto

  protected fun havingNsiStatusHistoryFeatureFlag(having: Boolean = true) =
    whenever(features.nsiStatusHistory).doReturn(having)

  protected fun havingSaveNsi() =
    whenever(nsiRepository.saveAndFlush(capture(captor)))
      .thenAnswer { it.arguments[0] }

  protected fun havingMappedNsi() {
    mapped = Fake.nsiDto()
    whenever(mapper.toDto(any<Nsi>())).thenReturn(mapped)
  }

  protected fun shouldReturnMapped() = assertThat(observed).isSameAs(mapped)

  protected fun shouldAssertSupportedTypeLevel() =
    verify(validation, times(1)).assertSupportedTypeLevel(captor.value)
}

class CreateNsiTest : NsiServiceTest() {
  private lateinit var request: NewNsi
  private lateinit var offender: Offender
  private lateinit var type: NsiType
  private lateinit var subType: StandardReference
  private lateinit var intendedProvider: Provider
  private lateinit var event: Event
  private lateinit var requirement: Requirement
  private lateinit var status: NsiStatus
  private lateinit var outcome: StandardReference

  @BeforeEach
  fun beforeEach() {
    request = Fake.newNsi()
  }

  @Test
  fun `Attempting to create nsi with missing offender`() {
    havingOffenderByCrn(false)
    assertThrows<BadRequestException> { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi with missing type`() {
    havingOffenderByCrn()
    havingTypeByCode(false)
    assertThrows<BadRequestException> { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi with missing sub type`() {
    havingOffenderByCrn()
    havingTypeByCode(havingSubType = false)
    assertThrows<BadRequestException> { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi without sub type but sub type required`() {
    havingOffenderByCrn()
    havingTypeByCode()
    request = request.copy(subType = null)
    assertThrows<BadRequestException>("NSI type '${type.code}' requires a sub type") { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi with missing intended provider`() {
    havingOffenderByCrn()
    havingTypeByCode(havingIntendedProvider = false)
    assertThrows<BadRequestException> { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi with missing event`() {
    havingOffenderByCrn(havingEvent = false)
    havingTypeByCode()
    assertThrows<BadRequestException> { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi with missing requirement`() {
    havingOffenderByCrn(havingRequirement = false)
    havingTypeByCode()
    assertThrows<BadRequestException> { whenCreatingNsi() }
  }

  @Test
  fun `Attempting to create nsi with referral date before event referral date`() {
    havingOffenderByCrn()
    havingTypeByCode()
    event.referralDate = request.referralDate.plusDays(1)
    assertThrows<BadRequestException>("Referral date must not be before the event referral date '${event.referralDate}'") {
      whenCreatingNsi()
    }
  }

  @Test
  fun `Successfully creating nsi`() {
    havingOffenderByCrn()
    havingTypeByCode()
    havingValidStatus()
    havingValidOutcome()
    havingSaveNsi()
    havingMappedNsi()
    havingNsiStatusHistoryFeatureFlag()

    whenCreatingNsi()

    shouldAssertTypeConstraints()
    shouldValidateRequirement()
    shouldCreateNsiManager()
    shouldAssertSupportedTypeLevel()
    shouldCreateSystemContacts()
    shouldReturnMapped()
    shouldSetNsiProperties()
    shouldSetStatusHistory()
  }

  @Test
  fun `Successfully creating nsi without event or requirement`() {
    havingOffenderByCrn()
    request = request.copy(eventId = null, requirementId = null)
    havingTypeByCode()
    havingValidStatus()
    havingValidOutcome()
    havingSaveNsi()
    havingMappedNsi()
    havingNsiStatusHistoryFeatureFlag()

    whenCreatingNsi()

    shouldAssertTypeConstraints()
    shouldCreateNsiManager()
    shouldAssertSupportedTypeLevel()
    shouldCreateSystemContacts()
    shouldReturnMapped()

    assertThat(captor.value)
      .hasProperty(Nsi::event, null)
      .hasProperty(Nsi::requirement, null)
  }

  @Test
  fun `Successfully creating nsi with status history disabled`() {
    havingOffenderByCrn()
    havingTypeByCode()
    havingValidStatus()
    havingValidOutcome()
    havingSaveNsi()
    havingMappedNsi()
    havingNsiStatusHistoryFeatureFlag(false)

    whenCreatingNsi()

    shouldAssertTypeConstraints()
    shouldValidateRequirement()
    shouldCreateNsiManager()
    shouldAssertSupportedTypeLevel()
    shouldCreateSystemContacts()
    shouldReturnMapped()
    shouldSetNsiProperties()
    shouldNotSetStatusHistory()
  }

  private fun havingOffenderByCrn(
    having: Boolean = true,
    havingEvent: Boolean = true,
    havingRequirement: Boolean = true,
  ) {
    requirement = Fake.requirement().apply {
      id = request.requirementId!!
      offenderId = Fake.id()
    }
    event = Fake.event().apply {
      id = request.eventId!!
      disposal!!.requirements = if (havingRequirement) listOf(requirement) else emptyList()
      referralDate = request.referralDate.minusDays(1)
    }
    offender = Fake.offender().apply {
      id = requirement.offenderId
      events = if (havingEvent) listOf(event) else emptyList()
    }
    whenever(offenderRepository.findByCrn(request.offenderCrn))
      .thenReturn(if (having) offender else null)
  }

  private fun havingTypeByCode(
    having: Boolean = true,
    havingSubType: Boolean = true,
    havingIntendedProvider: Boolean = true,
  ) {
    subType = Fake.standardReference().apply {
      code = request.subType!!
    }
    intendedProvider = Fake.provider().apply { code = request.intendedProvider }
    type = Fake.nsiType().apply {
      subTypes = if (havingSubType) listOf(subType) else emptyList()
      providers = if (havingIntendedProvider) listOf(intendedProvider) else emptyList()
    }
    whenever(nsiTypeRepository.findByCode(request.type))
      .thenReturn(if (having) type else null)
  }

  private fun havingValidStatus() {
    status = Fake.nsiStatus()
    whenever(validation.validateStatus(type, request)).thenReturn(status)
  }

  private fun havingValidOutcome() {
    outcome = Fake.standardReference()
    whenever(validation.validateOutcome(type, request)).thenReturn(outcome)
  }

  private fun whenCreatingNsi() {
    observed = subject.createNsi(request)
  }

  private fun shouldAssertTypeConstraints() =
    verify(validation, times(1)).assertTypeConstraints(type, request)

  private fun shouldValidateRequirement() =
    verify(validation, times(1)).assertRequirementConstraints(type, requirement, request)

  private fun shouldCreateNsiManager() =
    verify(nsiManagerService, times(1)).createNsiManager(captor.value, request.manager, request.referralDate)

  private fun shouldCreateSystemContacts() {
    verify(nsiSystemContactService, times(1)).createReferralContact(captor.value)
    verify(nsiSystemContactService, times(1)).createStatusContact(captor.value)
    verify(nsiSystemContactService, times(1)).createCommencedContact(captor.value)
    verify(nsiSystemContactService, times(1)).createTerminationContact(captor.value)
  }

  private fun shouldSetNsiProperties() =
    assertThat(captor.value)
      .hasProperty(Nsi::offender, offender)
      .hasProperty(Nsi::event, event)
      .hasProperty(Nsi::type, type)
      .hasProperty(Nsi::subType, subType)
      .hasProperty(Nsi::length, request.length)
      .hasProperty(Nsi::referralDate, request.referralDate)
      .hasProperty(Nsi::expectedStartDate, request.expectedStartDate)
      .hasProperty(Nsi::expectedEndDate, request.expectedEndDate)
      .hasProperty(Nsi::startDate, request.startDate)
      .hasProperty(Nsi::endDate, request.endDate)
      .hasProperty(Nsi::status, status)
      .hasProperty(Nsi::statusDate, request.statusDate)
      .hasProperty(Nsi::notes, request.notes)
      .hasProperty(Nsi::outcome, outcome)
      .hasProperty(Nsi::requirement, requirement)
      .hasProperty(Nsi::intendedProvider, intendedProvider)
      .hasProperty(Nsi::active, false)
      .hasProperty(Nsi::pendingTransfer, false)

  private fun shouldSetStatusHistory() =
    assertThat(captor.value.statuses)
      .hasSize(1)
      .element(0)
      .hasProperty(NsiStatusHistory::nsi, captor.value)
      .hasProperty(NsiStatusHistory::status, status)
      .hasProperty(NsiStatusHistory::date, request.statusDate)
      .hasProperty(NsiStatusHistory::notes, request.notes)

  private fun shouldNotSetStatusHistory() = assertThat(captor.value.statuses).isEmpty()
}

class UpdateNsiTest : NsiServiceTest() {
  private lateinit var originalNotes: String
  private lateinit var nsi: Nsi
  private lateinit var request: UpdateNsi
  private lateinit var newStatus: NsiStatus
  private lateinit var newOutcome: StandardReference

  @BeforeEach
  fun beforeEach() {
    request = Fake.updateNsi()
  }

  @Test
  fun `Attempting to update missing nsi`() {
    havingNsi(false)
    assertThrows<NotFoundException> { whenUpdatingNsi() }
  }

  @Test
  fun `Attempting to update nsi with old status date`() {
    havingNsi()
    nsi.statusDate = request.statusDate.plusSeconds(1)
    assertThrows<BadRequestException>("Updated status date must be equal to or after existing status date '${nsi.statusDate}'") {
      whenUpdatingNsi()
    }
  }

  @Test
  fun `Successfully updating nsi`() {
    havingNsiStatusHistoryFeatureFlag()
    havingNsi()
    havingNewStatus()
    havingNewOutcome()
    havingSaveNsi()
    havingMappedNsi()

    whenUpdatingNsi()

    shouldAssertRequirementConstraints()
    shouldAssertTypeConstraints()
    shouldCreateSystemContacts()
    shouldUpdateNsiManager()
    shouldAssertSupportedTypeLevel()
    shouldReturnMapped()
    shouldUpdateNsi()
    shouldUpdateNsiStatusHistory()
  }

  @Test
  fun `Successfully updating nsi with status history feature disabled`() {
    havingNsiStatusHistoryFeatureFlag(false)
    havingNsi()
    havingNewStatus()
    havingNewOutcome()
    havingSaveNsi()
    havingMappedNsi()

    whenUpdatingNsi()

    assertThat(nsi.statuses).isEmpty()
  }

  @Test
  fun `Successfully updating nsi without requirement`() {
    havingNsiStatusHistoryFeatureFlag()
    havingNsi()
    nsi.requirement = null
    havingNewStatus()
    havingNewOutcome()
    havingSaveNsi()
    havingMappedNsi()

    whenUpdatingNsi()

    shouldNotAssertRequirementConstraints()
    shouldAssertTypeConstraints()
    shouldCreateSystemContacts()
    shouldUpdateNsiManager()
    shouldAssertSupportedTypeLevel()
    shouldReturnMapped()
    shouldUpdateNsi()
    shouldUpdateNsiStatusHistory()
  }

  private fun havingNsi(having: Boolean = true) {
    nsi = Fake.nsi().apply {
      statusDate = request.statusDate.minusSeconds(1)
      originalNotes = notes!!
    }
    whenever(nsiRepository.findById(nsi.id))
      .thenReturn(if (having) Optional.of(nsi) else Optional.empty())
  }

  private fun havingNewStatus() {
    newStatus = Fake.nsiStatus()
    whenever(validation.validateStatus(nsi.type, request)).thenReturn(newStatus)
  }

  private fun havingNewOutcome() {
    newOutcome = Fake.standardReference()
    whenever(validation.validateOutcome(nsi.type, request)).thenReturn(newOutcome)
  }

  private fun whenUpdatingNsi() {
    observed = subject.updateNsi(nsi.id, request)
  }

  private fun shouldAssertTypeConstraints() =
    verify(validation, times(1)).assertTypeConstraints(nsi.type, request)

  private fun shouldAssertRequirementConstraints() =
    verify(validation, times(1)).assertRequirementConstraints(nsi.type, nsi.requirement!!, request)

  private fun shouldNotAssertRequirementConstraints() =
    verify(validation, never()).assertRequirementConstraints(any(), any(), any())

  private fun shouldCreateSystemContacts() {
    verify(nsiSystemContactService, times(1)).updateCommencedContact(nsi, request)
    verify(nsiSystemContactService, times(1)).updateStatusContact(nsi, newStatus, request)
    verify(nsiSystemContactService, times(1)).updateTerminationContact(nsi, newOutcome, request)
  }

  private fun shouldUpdateNsiManager() =
    verify(nsiManagerService, times(1)).updateNsiManager(captor.value, request.manager)

  private fun shouldUpdateNsi() =
    assertThat(nsi)
      .hasProperty(Nsi::startDate, request.startDate)
      .hasProperty(Nsi::endDate, request.endDate)
      .hasProperty(Nsi::active, false)
      .hasProperty(Nsi::expectedStartDate, request.expectedStartDate)
      .hasProperty(Nsi::expectedEndDate, request.expectedEndDate)
      .hasProperty(Nsi::length, request.length)
      .hasProperty(Nsi::status, newStatus)
      .hasProperty(Nsi::statusDate, request.statusDate)
      .hasProperty(Nsi::outcome, newOutcome)
      .hasProperty(Nsi::notes, originalNotes + "\n" + request.notes)

  private fun shouldUpdateNsiStatusHistory() {
    val description = nsi.statuses.joinToString(", ") { s -> s.status!!.code }
    assertThat(nsi.statuses)
      .describedAs("should have new status: $description")
      .anyMatch { it.status!!.code == newStatus.code }
  }
}
