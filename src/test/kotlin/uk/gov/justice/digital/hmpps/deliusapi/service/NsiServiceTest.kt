package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ReferenceDataMasterRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TransferReasonRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
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
  @Captor private lateinit var entityCaptor: ArgumentCaptor<Nsi>
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

  @Test
  fun `Successfully creating nsi`() {
    havingDependentEntities()
    val created = Fake.nsi()
    whenever(nsiRepository.saveAndFlush(entityCaptor.capture())).thenReturn(created)
    val observed = subject.createNsi(request)
    assertThat(observed)
      .isInstanceOf(NsiDto::class.java)
      .hasProperty(NsiDto::id, created.id)

    assertThat(entityCaptor.value)
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

    assertThat(entityCaptor.value.managers)
      .hasSize(1)
      .element(0)
      .hasProperty(NsiManager::provider, managerProvider)
      .hasProperty(NsiManager::team, managerTeam)
      .hasProperty(NsiManager::staff, managerStaff)

    shouldSetAuditContext(created.id)
  }

  @Test
  fun `Attempting to create offender level nsi on event level only type`() {
    havingDependentEntities(havingOffenderLevel = false)
    request = request.copy(eventId = null, requirementId = null)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create event level nsi on offender level only type`() {
    havingDependentEntities(havingEventLevel = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing offender`() {
    havingDependentEntities(havingOffender = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing event`() {
    havingDependentEntities(havingEvent = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing requirement`() {
    havingDependentEntities(havingRequirement = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing type`() {
    havingDependentEntities(havingType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing sub type`() {
    havingDependentEntities(havingSubType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing status`() {
    havingDependentEntities(havingStatus = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing intended provider`() {
    havingDependentEntities(havingIntendedProvider = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing outcome`() {
    havingDependentEntities(havingOutcome = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing manager provider`() {
    havingDependentEntities(havingManagerProvider = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing manager team`() {
    havingDependentEntities(havingManagerTeam = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with missing manager staff`() {
    havingDependentEntities(havingManagerStaff = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi when type does not allow active duplicates`() {
    havingDependentEntities(havingActiveDuplicates = false)
    request = request.copy(endDate = null)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi when type does not allow inactive duplicates`() {
    havingDependentEntities(havingInactiveDuplicates = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi without length when units are required`() {
    havingDependentEntities()
    request = request.copy(length = null)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with length larger than maximum`() {
    havingDependentEntities()
    request = request.copy(length = type.maximumLength!! + 1)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with length smaller than minimum`() {
    havingDependentEntities()
    request = request.copy(length = type.minimumLength!! - 1)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with length when units are not required`() {
    havingDependentEntities(havingUnits = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi with referral date before event date`() {
    havingDependentEntities()
    request = request.copy(referralDate = event.referralDate.minusDays(1))
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi on terminated requirement without end date`() {
    havingDependentEntities()
    request = request.copy(endDate = null)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create nsi on terminated requirement with end date before requirement termination date`() {
    havingDependentEntities()
    request = request.copy(endDate = requirement.terminationDate!!.minusDays(1))
    shouldThrowBadRequest()
  }

  fun shouldThrowBadRequest() = assertThrows<BadRequestException> {
    subject.createNsi(request)
  }

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

    requirement = Fake.requirement().copy(
      id = request.requirementId!!,
      offenderId = offenderId,
      terminationDate = request.endDate
    )
    val requirements = if (havingRequirement) listOf(requirement, Fake.requirement()) else listOf()

    event = Fake.event().copy(
      id = request.eventId!!,
      referralDate = request.referralDate,
      disposals = listOf(Fake.disposal().copy(requirements = requirements))
    )
    val events = if (havingEvent) listOf(event, Fake.event()) else listOf()

    offender = Fake.offender().copy(id = offenderId, events = events)
    whenever(offenderRepository.findByCrn(request.offenderCrn))
      .thenReturn(if (havingOffender) offender else null)

    subType = Fake.standardReference().copy(code = request.subType!!)
    status = Fake.nsiStatus().copy(code = request.status)
    intendedProvider = Fake.provider().copy(code = request.intendedProvider)
    outcome = Fake.standardReference().copy(code = request.outcome!!)

    type = Fake.nsiType().copy(
      subTypes = if (havingSubType) listOf(subType, Fake.standardReference()) else listOf(),
      statuses = if (havingStatus) listOf(status, Fake.nsiStatus()) else listOf(),
      providers = if (havingIntendedProvider) listOf(intendedProvider, Fake.provider()) else listOf(),
      units = if (havingUnits) Fake.standardReference() else null,
      outcomes = if (havingOutcome) listOf(outcome, Fake.standardReference()) else listOf(),
      eventLevel = havingEventLevel,
      offenderLevel = havingOffenderLevel,
      allowActiveDuplicates = havingActiveDuplicates,
      allowInactiveDuplicates = havingInactiveDuplicates,
    )
    whenever(nsiTypeRepository.findByCode(request.type))
      .thenReturn(if (havingType) type else null)

    managerStaff = Fake.staff().copy(code = request.manager.staff)
    managerTeam = Fake.team().copy(
      code = request.manager.team,
      staff = if (havingManagerStaff) listOf(managerStaff, Fake.staff()) else listOf()
    )
    managerProvider = Fake.provider().copy(
      code = request.manager.provider,
      teams = if (havingManagerTeam) listOf(managerTeam, Fake.team()) else listOf()
    )
    whenever(providerRepository.findByCode(request.manager.provider))
      .thenReturn(if (havingManagerProvider) managerProvider else null)

    whenever(transferReasonRepository.findByCode("NSI"))
      .thenReturn(Fake.transferReason())

    val referenceMaster = Fake.referenceDataMaster().copy(
      standardReferences = listOf(Fake.standardReference().copy(code = "IN1"))
    )
    whenever(referenceDataMasterRepository.findByCode("NM ALLOCATION REASON"))
      .thenReturn(referenceMaster)
  }

  private fun shouldSetAuditContext(id: Long) {
    val context = AuditContext.get(AuditableInteraction.ADMINISTER_NSI)
    assertThat(context)
      .hasProperty(AuditContext::offenderId, offender.id)
      .hasProperty(AuditContext::nsiId, id)
  }
}
