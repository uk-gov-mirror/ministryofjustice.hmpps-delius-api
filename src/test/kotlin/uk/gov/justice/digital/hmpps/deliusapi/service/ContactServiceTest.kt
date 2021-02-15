package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
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
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactOutcomeTypeRepository
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
  private lateinit var contactOutcomeTypeRepository: ContactOutcomeTypeRepository

  @Mock
  private lateinit var providerRepository: ProviderRepository

  @Mock
  private lateinit var auditService: AuditService

  @InjectMocks
  private lateinit var subject: ContactService

  private lateinit var newContact: NewContact
  private lateinit var offender: Offender
  private lateinit var type: ContactType
  private lateinit var outcome: ContactOutcomeType

  @BeforeEach
  fun beforeEach() {
    newContact = Fake.newContact()
    offender = Fake.offender()
    type = Fake.contactType()
    outcome = Fake.contactOutcomeType()
  }

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
  fun `Attempting to create contact with missing outcome`() {
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
  fun `Attempting to create contact with type not matching outcome`() {
    havingDependentEntities()
    newContact = newContact.copy(contactOutcome = "INVALID")
    shouldThrowBadRequest()
  }

  fun havingDependentEntities(
    havingOffender: Boolean = true,
    havingType: Boolean = true,
    havingOutcome: Boolean = true,
    havingProvider: Boolean = true,
    havingOfficeLocation: Boolean = true,
    havingTeam: Boolean = true,
    havingStaff: Boolean = true,
  ) {
    whenever(offenderRepository.findByCrn(newContact.offenderCrn))
      .thenReturn(if (havingOffender) offender else null)

    whenever(contactTypeRepository.findByCode(newContact.contactType))
      .thenReturn(if (havingType) type else null)

    whenever(contactOutcomeTypeRepository.findByCode(newContact.contactOutcome))
      .thenReturn(if (havingOutcome) outcome else null)

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
