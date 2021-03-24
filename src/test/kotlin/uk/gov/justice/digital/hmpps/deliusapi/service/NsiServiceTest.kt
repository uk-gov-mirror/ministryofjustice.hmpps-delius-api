package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatusHistory
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ReferenceDataMasterRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TransferReasonRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.NewSystemContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.SystemContactService
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import uk.gov.justice.digital.hmpps.deliusapi.service.nsi.NsiService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NsiServiceTest {

  @Mock private lateinit var nsiRepository: NsiRepository
  @Mock private lateinit var offenderRepository: OffenderRepository
  @Mock private lateinit var nsiTypeRepository: NsiTypeRepository
  @Mock private lateinit var providerRepository: ProviderRepository
  @Mock private lateinit var transferReasonRepository: TransferReasonRepository
  @Mock private lateinit var referenceDataMasterRepository: ReferenceDataMasterRepository
  @Mock private lateinit var systemContactService: SystemContactService
  @Mock private lateinit var mapper: NsiMapper
  @Spy private lateinit var features: FeatureFlags
  @Captor private lateinit var newNsiCaptor: ArgumentCaptor<Nsi>
  @Captor private lateinit var statusContactCaptor: ArgumentCaptor<NewSystemContact>
  @InjectMocks private lateinit var subject: NsiService

  private lateinit var request: NewNsi
  private lateinit var type: NsiType
  private lateinit var subType: StandardReference
  private lateinit var status: NsiStatus
  private lateinit var intendedProvider: Provider
  private lateinit var outcome: StandardReference
  private lateinit var offender: Offender
  private lateinit var event: Event
  private lateinit var requirement: Requirement
  private lateinit var managerProvider: Provider
  private lateinit var managerStaff: Staff
  private lateinit var managerTeam: Team
  private lateinit var created: Nsi

  @Test
  fun `Successfully creating nsi`() {
    havingNsiStatushistoryFeatureFlag()
    havingDependentEntities()
    whenSuccessfullyCreatingNsi()
    shouldSetNsiProperties()
    shouldSetAuditContext()
    shouldCreateSystemContacts()
  }

  @Test
  fun `Successfully creating nsi with status history flag disabled`() {
    havingNsiStatushistoryFeatureFlag(false)
    havingDependentEntities()
    whenSuccessfullyCreatingNsi()
    shouldSetNsiProperties(shouldSetStatus = false)
    shouldSetAuditContext()
    shouldCreateSystemContacts()
  }

  @Test
  fun `Attempting to create offender level nsi on event level only type`() {
    havingDependentEntities(havingOffenderLevel = false)
    request = request.copy(eventId = null, requirementId = null)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create event level nsi on offender level only type`() {
    havingDependentEntities(havingEventLevel = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing offender`() {
    havingDependentEntities(havingOffender = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing event`() {
    havingDependentEntities(havingEvent = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing requirement`() {
    havingDependentEntities(havingRequirement = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing type`() {
    havingDependentEntities(havingType = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing sub type`() {
    havingDependentEntities(havingSubType = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing status`() {
    havingDependentEntities(havingStatus = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing intended provider`() {
    havingDependentEntities(havingIntendedProvider = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing outcome`() {
    havingDependentEntities(havingOutcome = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing manager provider`() {
    havingDependentEntities(havingManagerProvider = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing manager team`() {
    havingDependentEntities(havingManagerTeam = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with missing manager staff`() {
    havingDependentEntities(havingManagerStaff = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi when type does not allow active duplicates`() {
    havingDependentEntities(havingActiveDuplicates = false)
    request = request.copy(endDate = null)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi when type does not allow inactive duplicates`() {
    havingDependentEntities(havingInactiveDuplicates = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi without length when units are required`() {
    havingDependentEntities()
    request = request.copy(length = null)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with length larger than maximum`() {
    havingDependentEntities()
    request = request.copy(length = type.maximumLength!! + 1)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with length smaller than minimum`() {
    havingDependentEntities()
    request = request.copy(length = type.minimumLength!! - 1)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with length when units are not required`() {
    havingDependentEntities(havingUnits = false)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi with referral date before event date`() {
    havingDependentEntities()
    request = request.copy(referralDate = event.referralDate.minusDays(1))
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi on terminated requirement without end date`() {
    havingDependentEntities()
    request = request.copy(endDate = null)
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  @Test
  fun `Attempting to create nsi on terminated requirement with end date before requirement termination date`() {
    havingDependentEntities()
    request = request.copy(endDate = requirement.terminationDate!!.minusDays(1))
    shouldThrowBadRequestAndNotCreateAnySystemContacts()
  }

  private fun havingNsiStatushistoryFeatureFlag(having: Boolean = true) =
    whenever(features.nsiStatusHistory).doReturn(having)

  private fun havingDependentEntities(
    havingOffender: Boolean = true,
    havingEvent: Boolean = true,
    havingRequirement: Boolean = true,
    havingType: Boolean = true,
    havingSubType: Boolean = true,
    havingStatus: Boolean = true,
    havingIntendedProvider: Boolean = true,
    havingUnits: Boolean = true,
    havingOutcome: Boolean = true,
    havingManagerProvider: Boolean = true,
    havingManagerTeam: Boolean = true,
    havingManagerStaff: Boolean = true,
    havingEventLevel: Boolean = true,
    havingOffenderLevel: Boolean = true,
    havingActiveDuplicates: Boolean = true,
    havingInactiveDuplicates: Boolean = true,
  ) {
    request = Fake.newNsi()

    val offenderId = Fake.id()

    subType = Fake.standardReference().apply {
      code = request.subType!!
    }

    status = Fake.nsiStatus().apply {
      code = request.status
    }
    intendedProvider = Fake.provider().apply {
      code = request.intendedProvider
    }
    outcome = Fake.standardReference().apply {
      code = request.outcome!!
    }

    type = Fake.nsiType().apply {
      subTypes = if (havingSubType) listOf(subType, Fake.standardReference()) else listOf()
      statuses = if (havingStatus) listOf(status, Fake.nsiStatus()) else listOf()
      providers = if (havingIntendedProvider) listOf(intendedProvider, Fake.provider()) else listOf()
      units = if (havingUnits) Fake.standardReference() else null
      outcomes = if (havingOutcome) listOf(outcome, Fake.standardReference()) else listOf()
      eventLevel = havingEventLevel
      offenderLevel = havingOffenderLevel
      allowActiveDuplicates = havingActiveDuplicates
      allowInactiveDuplicates = havingInactiveDuplicates
    }

    whenever(nsiTypeRepository.findByCode(request.type))
      .thenReturn(if (havingType) type else null)

    requirement = Fake.requirement().apply {
      id = request.requirementId!!
      this.offenderId = offenderId
      terminationDate = request.endDate
      typeCategory!!.nsiTypes = listOf(type)
    }

    val requirements = if (havingRequirement) listOf(requirement, Fake.requirement().apply { }) else listOf()

    val disposal = Fake.disposal().apply { this.requirements = requirements }

    event = Fake.event().apply {
      id = request.eventId!!
      referralDate = request.referralDate
      disposals = listOf(disposal)
    }
    val events = if (havingEvent) listOf(event, Fake.event()) else listOf()

    offender = Fake.offender()
    offender.id = offenderId
    offender.events = events

    whenever(offenderRepository.findByCrn(request.offenderCrn))
      .thenReturn(if (havingOffender) offender else null)

    managerStaff = Fake.staff().apply { code = request.manager.staff!! }

    managerTeam = Fake.team().apply {
      code = request.manager.team!!
      staff = if (havingManagerStaff) listOf(managerStaff, Fake.staff()) else listOf()
    }

    managerProvider = Fake.provider().apply {
      code = request.manager.provider
      teams = if (havingManagerTeam) listOf(managerTeam, Fake.team()) else listOf()
    }

    whenever(providerRepository.findByCodeAndSelectableIsTrue(request.manager.provider))
      .thenReturn(if (havingManagerProvider) managerProvider else null)

    whenever(transferReasonRepository.findByCode("NSI"))
      .thenReturn(Fake.transferReason())

    val reference = Fake.standardReference().apply { code = "IN1" }

    val referenceMaster = Fake.referenceDataMaster().apply { standardReferences = listOf(reference) }

    whenever(referenceDataMasterRepository.findByCode("NM ALLOCATION REASON"))
      .thenReturn(referenceMaster)

    created = Fake.nsi().also {
      it.type = type
      it.subType = subType
      it.status = status
      it.intendedProvider = intendedProvider
      it.outcome = outcome
      it.offender = offender
      it.event = event
      it.requirement = requirement
      it.managers = mutableListOf(
        Fake.nsiManager().apply {
          provider = managerProvider
          team = managerTeam
          staff = managerStaff
        }
      )
      it.statusDate = request.statusDate
    }
  }

  private fun whenSuccessfullyCreatingNsi() {
    val expectedResult = Fake.nsiDto()

    whenever(nsiRepository.saveAndFlush(newNsiCaptor.capture())).thenReturn(created)
    whenever(mapper.toDto(created)).thenReturn(expectedResult)

    val observed = subject.createNsi(request)
    assertThat(observed).isSameAs(expectedResult)
  }

  private fun shouldSetNsiProperties(shouldSetStatus: Boolean = true) {
    assertThat(newNsiCaptor.value)
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
      .hasProperty(Nsi::active, false)
      .hasProperty(Nsi::pendingTransfer, false)
      .hasProperty(Nsi::requirement, requirement)
      .hasProperty(Nsi::intendedProvider, intendedProvider)
      .hasProperty(Nsi::softDeleted, false)

    assertThat(newNsiCaptor.value.managers)
      .hasSize(1)
      .element(0)
      .hasProperty(NsiManager::provider, managerProvider)
      .hasProperty(NsiManager::team, managerTeam)
      .hasProperty(NsiManager::staff, managerStaff)
      .hasProperty(NsiManager::nsi, newNsiCaptor.value)

    if (shouldSetStatus) {
      assertThat(newNsiCaptor.value.statuses)
        .hasSize(1)
        .element(0)
        .hasProperty(NsiStatusHistory::nsi, newNsiCaptor.value)
        .hasProperty(NsiStatusHistory::nsiStatus, status)
        .hasProperty(NsiStatusHistory::date, request.statusDate)
        .hasProperty(NsiStatusHistory::notes, request.notes)
    }
  }

  private fun shouldCreateSystemContacts() {
    verify(systemContactService, times(4)).createSystemContact(capture(statusContactCaptor))

    shouldCreateSystemContact(created.id, typeId = status.contactTypeId)
    shouldCreateSystemContact(created.id, wellKnownType = WellKnownContactType.REFERRAL)
    shouldCreateSystemContact(created.id, wellKnownType = WellKnownContactType.COMMENCED)
    shouldCreateSystemContact(
      created.id,
      wellKnownType = WellKnownContactType.TERMINATED,
      notes = "NSI Terminated with Outcome: ${outcome.description}"
    )
  }

  private fun shouldSetAuditContext() {
    val context = AuditContext.get(AuditableInteraction.ADMINISTER_NSI)
    assertThat(context)
      .hasProperty(AuditContext::offenderId, offender.id)
      .hasProperty(AuditContext::nsiId, created.id)
  }

  private fun shouldCreateSystemContact(
    nsiId: Long,
    typeId: Long? = null,
    wellKnownType: WellKnownContactType? = null,
    notes: String? = null
  ) {

    val observed = statusContactCaptor.allValues.find {
      when {
        typeId != null -> it.typeId == typeId
        wellKnownType != null -> it.type == wellKnownType
        else -> false
      }
    }
    assertThat(observed)
      .describedAs("should create system contact with typeId = '$typeId' or typeCode = '$wellKnownType'")
      .isNotNull

    assertThat(observed!!)
      .hasProperty(NewSystemContact::typeId, typeId)
      .hasProperty(NewSystemContact::type, wellKnownType)
      .hasProperty(NewSystemContact::offenderId, offender.id)
      .hasProperty(NewSystemContact::nsiId, nsiId)
      .hasProperty(NewSystemContact::eventId, event.id)
      .hasProperty(NewSystemContact::providerId, managerProvider.id)
      .hasProperty(NewSystemContact::teamId, managerTeam.id)
      .hasProperty(NewSystemContact::staffId, managerStaff.id)
      .hasProperty(NewSystemContact::timestamp, request.statusDate)
      .hasProperty(NewSystemContact::notes, notes)
  }

  private fun shouldNotCreateSystemContact() {
    verifyZeroInteractions(systemContactService)
  }

  private fun shouldThrowBadRequestAndNotCreateAnySystemContacts() {
    assertThrows<BadRequestException> { subject.createNsi(request) }
    shouldNotCreateSystemContact()
  }
}
