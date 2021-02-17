package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditService
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
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

  @Mock
  private lateinit var auditService: AuditService

  @InjectMocks
  private lateinit var subject: ContactService

  private lateinit var newContact: NewContact
  private lateinit var type: ContactType
  private lateinit var outcome: ContactOutcomeType
  private lateinit var offender: Offender

  @Test
  fun `Creating contact`() {
    val savedContact = Fake.contact()

    havingDependentEntities()
    whenever(contactRepository.saveAndFlush(any())).thenReturn(savedContact)

    val observed = subject.createContact(newContact)
    Assertions.assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .hasFieldOrPropertyWithValue("id", savedContact.id)

    verify(auditService).successfulInteraction(1, AuditableInteraction.ADD_CONTACT, offender.id)
  }

  @Test
  fun `Creating future contact with acceptable absence outcome`() {
    val savedContact = Fake.contact()

    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomFutureLocalDate())

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(outcomeTypes = listOf(outcome.copy(attendance = false, compliantAcceptable = true))))
    whenever(contactRepository.saveAndFlush(any())).thenReturn(savedContact)

    subject.createContact(newContact)

    verify(contactRepository).saveAndFlush(any())
  }

  @Test
  fun `Creating future contact with no outcome`() {
    val savedContact = Fake.contact()
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomFutureLocalDate(), outcome = null)

    whenever(contactRepository.saveAndFlush(any())).thenReturn(savedContact)

    subject.createContact(newContact)

    verify(contactRepository).saveAndFlush(any())
  }

  @Test
  fun `Creating historic contact with no outcome when outcome not required`() {
    val savedContact = Fake.contact()
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomPastLocalDate(), outcome = null)

    whenever(contactTypeRepository.findByCode(newContact.type)).thenReturn(type.copy(outcomeFlag = false))
    whenever(contactRepository.saveAndFlush(any())).thenReturn(savedContact)

    subject.createContact(newContact)

    verify(contactRepository).saveAndFlush(any())
  }

  @Test
  fun `Attempting to create historic contact with no outcome when outcome required`() {
    havingDependentEntities()
    newContact = newContact.copy(date = Fake.randomPastLocalDate(), outcome = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Error saving contact`() {
    havingDependentEntities()
    whenever(contactRepository.saveAndFlush(any())).thenThrow(RuntimeException::class.java)

    assertThrows<RuntimeException> { subject.createContact(newContact) }

    verify(auditService).failedInteraction(1, AuditableInteraction.ADD_CONTACT, offender.id)
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

  fun havingDependentEntities(
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

    val offenderId = Fake.faker.number().randomNumber()
    val requirements = if (havingRequirement) listOf(Fake.requirement(id = newContact.requirementId, offenderId = offenderId), Fake.requirement()) else listOf()
    val disposals = listOf(Fake.disposal(requirements = requirements))
    val events = if (havingEvent) listOf(Fake.event(id = newContact.eventId, disposals = disposals), Fake.event()) else listOf()
    offender = Fake.offender(id = offenderId, events = events)
    whenever(offenderRepository.findByCrn(newContact.offenderCrn))
      .thenReturn(if (havingOffender) offender else null)

    outcome = Fake.contactOutcomeType(code = newContact.outcome)
    type = Fake.contactType(outcomeTypes = if (havingOutcome) listOf(outcome, Fake.contactOutcomeType()) else listOf())
    whenever(contactTypeRepository.findByCode(newContact.type))
      .thenReturn(if (havingType) type else null)

    val staff = if (havingStaff) listOf(Fake.staff(code = newContact.staff), Fake.staff()) else listOf()
    val teams = if (havingTeam) listOf(Fake.team(code = newContact.team, staff = staff), Fake.team()) else listOf()
    val officeLocations = if (havingOfficeLocation) listOf(Fake.officeLocation(code = newContact.officeLocation, teams = teams), Fake.officeLocation()) else listOf()
    val provider = if (havingProvider) Fake.provider(code = newContact.provider, officeLocations = officeLocations) else null

    whenever(providerRepository.findByCode(newContact.provider)).thenReturn(provider)
  }

  fun shouldThrowBadRequest() = assertThrows<BadRequestException> {
    subject.createContact(newContact)
  }
}
