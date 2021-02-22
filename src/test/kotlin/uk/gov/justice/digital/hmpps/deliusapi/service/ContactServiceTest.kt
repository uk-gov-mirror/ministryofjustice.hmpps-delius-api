package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.B
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.N
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContactServiceTest {

  @Mock
  private lateinit var contactRepository: ContactRepository

  @Mock
  private lateinit var offenderRepository: OffenderRepository

  @Mock
  private lateinit var contactTypeRepository: ContactTypeRepository

  @Mock
  private lateinit var providerRepository: ProviderRepository

  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>

  @InjectMocks
  private lateinit var subject: ContactService

  private lateinit var newContact: NewContact
  private lateinit var type: ContactType
  private lateinit var outcome: ContactOutcomeType
  private lateinit var offender: Offender
  private lateinit var provider: Provider
  private lateinit var staff: Staff
  private lateinit var team: Team
  private lateinit var officeLocation: OfficeLocation
  private lateinit var event: Event
  private lateinit var requirement: Requirement

  @Test
  fun `Creating contact`() {
    val savedContact = Fake.contact()

    havingDependentEntities()
    whenever(contactRepository.saveAndFlush(entityCaptor.capture())).thenReturn(savedContact)

    val observed = subject.createContact(newContact)
    assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .hasFieldOrPropertyWithValue("id", savedContact.id)

    val entity = entityCaptor.value
    assertThat(entity.offender).isSameAs(offender)
    assertThat(entity.type).isSameAs(type)
    assertThat(entity.outcome).isSameAs(outcome)
    assertThat(entity.provider).isSameAs(provider)
    assertThat(entity.team).isSameAs(team)
    assertThat(entity.staff).isSameAs(staff)
    assertThat(entity.event).isSameAs(event)
    assertThat(entity.requirement).isSameAs(requirement)
    assertThat(entity.officeLocation).isSameAs(officeLocation)
    assertThat(entity.date).isEqualTo(newContact.date)
    assertThat(entity.startTime).isEqualTo(newContact.startTime)
    assertThat(entity.endTime).isEqualTo(newContact.endTime)
    assertThat(entity.alert).isEqualTo(newContact.alert)
    assertThat(entity.sensitive).isEqualTo(newContact.sensitive)
    assertThat(entity.notes).isEqualTo(newContact.notes)
    assertThat(entity.description).isEqualTo(newContact.description)
    assertThat(entity.partitionAreaId).isEqualTo(0)
    assertThat(entity.staffEmployeeId).isEqualTo(1)
    assertThat(entity.teamProviderId).isEqualTo(1)
  }

  @Test
  fun `Creating future contact with acceptable absence outcome`() {
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomFutureLocalDate())

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(outcomeTypes = listOf(outcome.copy(attendance = false, compliantAcceptable = true))))

    passesValidation()
  }

  @Test
  fun `Creating future contact with no outcome`() {
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomFutureLocalDate(), outcome = null)

    passesValidation()
  }

  @Test
  fun `Creating historic contact with no outcome when outcome not required`() {
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomPastLocalDate(), outcome = null)

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(outcomeFlag = N))

    passesValidation()
  }

  @Test
  fun `Creating contact with no location when location not required`() {
    havingDependentEntities()
    newContact = newContact.copy(officeLocation = null)

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(locationFlag = N))

    passesValidation()
  }

  @Test
  fun `Creating contact with no location when location optional`() {
    havingDependentEntities()
    newContact = newContact.copy(officeLocation = null)

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(locationFlag = B))

    passesValidation()
  }

  @Test
  fun `Creating contact with location when location optional`() {
    havingDependentEntities()
    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(locationFlag = B))

    passesValidation()
  }

  @Test
  fun `Creating non-attendance and non-recordedHoursCredited contact without times`() {
    havingDependentEntities()

    newContact = newContact.copy(startTime = null, endTime = null)

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(attendanceContact = false, recordedHoursCredited = false))

    passesValidation()
  }

  @Test
  fun `Creating outcomed non-recordedHoursCredited contact without end time`() {
    havingDependentEntities()

    newContact = newContact.copy(endTime = null)

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(recordedHoursCredited = false))

    passesValidation()
  }

  @Test
  fun `Attempting to create outcomed recordedHoursCredited contact without end time`() {
    havingDependentEntities()

    newContact = newContact.copy(endTime = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create attendance contact without start time`() {
    havingDependentEntities()

    newContact = newContact.copy(startTime = null, endTime = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to creating contact with no location when location required`() {
    havingDependentEntities()
    newContact = newContact.copy(officeLocation = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create historic contact with no outcome when outcome required`() {
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomPastLocalDate(), outcome = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing offender`() {
    havingDependentEntities(havingOffender = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing type`() {
    havingDependentEntities(havingType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create historic contact with missing outcome`() {
    havingDependentEntities(havingOutcome = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing provider`() {
    havingDependentEntities(havingProvider = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing office location`() {
    havingDependentEntities(havingOfficeLocation = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing event`() {
    havingDependentEntities(havingEvent = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing requirement`() {
    havingDependentEntities(havingRequirement = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with type not matching outcome`() {
    havingDependentEntities()
    newContact = newContact.copy(outcome = "INVALID")
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with date in past and no contact outcome`() {
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomPastLocalDate(), outcome = null)
    shouldThrowBadRequest()
  }

  private fun havingDependentEntities(
    havingOffender: Boolean = true,
    havingEvent: Boolean = true,
    havingRequirement: Boolean = true,
    havingType: Boolean = true,
    havingOutcome: Boolean = true,
    havingProvider: Boolean = true,
    havingOfficeLocation: Boolean = true,
    havingTeam: Boolean = true,
    havingStaff: Boolean = true,
  ) {
    newContact = Fake.newContact()

    val offenderId = Fake.randomPositiveId()

    requirement = Fake.requirement(id = newContact.requirementId, offenderId = offenderId)
    val requirements = if (havingRequirement) listOf(requirement, Fake.requirement()) else listOf()

    event = Fake.event(id = newContact.eventId, disposals = listOf(Fake.disposal(requirements = requirements)))
    val events = if (havingEvent) listOf(event, Fake.event()) else listOf()

    offender = Fake.offender(id = offenderId, events = events)
    whenever(offenderRepository.findByCrn(newContact.offenderCrn))
      .thenReturn(if (havingOffender) offender else null)

    outcome = Fake.contactOutcomeType(code = newContact.outcome)
    type = Fake.contactType(outcomeTypes = if (havingOutcome) listOf(outcome, Fake.contactOutcomeType()) else listOf())
    whenever(contactTypeRepository.findByCode(newContact.type))
      .thenReturn(if (havingType) type else null)

    this.staff = Fake.staff(code = newContact.staff)
    val staff = if (havingStaff) listOf(this.staff, Fake.staff()) else listOf()

    officeLocation = Fake.officeLocation(code = newContact.officeLocation)
    val officeLocations = if (havingOfficeLocation) listOf(officeLocation, Fake.officeLocation()) else listOf()

    team = Fake.team(code = newContact.team, staff = staff, officeLocation = officeLocations)
    val teams = if (havingTeam) listOf(team, Fake.team()) else listOf()

    provider = Fake.provider(code = newContact.provider, teams = teams)
    whenever(providerRepository.findByCode(newContact.provider)).thenReturn(if (havingProvider) provider else null)
  }

  fun shouldThrowBadRequest() = assertThrows<BadRequestException> {
    subject.createContact(newContact)
  }

  private fun passesValidation() {
    whenever(contactRepository.saveAndFlush(any())).then(returnsFirstArg<Contact>())

    subject.createContact(newContact)

    verify(contactRepository).saveAndFlush(any())
  }
}
