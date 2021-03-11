package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.util.Optional

class CreateContactTest : ContactServiceTestBase() {
  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>
  private lateinit var request: NewContact

  @Test
  fun `Successfully creating contact`() {
    val savedContact = Fake.contact()
    val expectedResult = Fake.contactDto()

    havingDependentEntities()
    havingRepositories()
    havingValidation()

    whenever(contactRepository.saveAndFlush(entityCaptor.capture()))
      .thenReturn(savedContact)
    whenever(mapper.toDto(savedContact)).thenReturn(expectedResult)

    val observed = subject.createContact(request)
    assertThat(observed).isSameAs(expectedResult)

    assertThat(entityCaptor.value)
      .hasProperty(Contact::offender, offender)
      .hasProperty(Contact::type, type)
      .hasProperty(Contact::outcome, outcome)
      .hasProperty(Contact::provider, provider)
      .hasProperty(Contact::team, team)
      .hasProperty(Contact::staff, staff)
      .hasProperty(Contact::event, event)
      .hasProperty(Contact::requirement, requirement)
      .hasProperty(Contact::officeLocation, officeLocation)
      .hasProperty(Contact::date, request.date)
      .hasProperty(Contact::startTime, request.startTime)
      .hasProperty(Contact::endTime, request.endTime)
      .hasProperty(Contact::alert, request.alert)
      .hasProperty(Contact::sensitive, request.sensitive)
      .hasProperty(Contact::notes, type.defaultHeadings + ContactService.NOTES_SEPARATOR + request.notes)
      .hasProperty(Contact::description, request.description)
      .hasProperty(Contact::partitionAreaId, 0)
      .hasProperty(Contact::staffEmployeeId, 1)
      .hasProperty(Contact::teamProviderId, 1)

    shouldSetAuditContext()
  }

  @Test
  fun `Creating nsi contact`() {
    havingDependentEntities()
    type = type.copy(nsiTypes = listOf(nsi.type?.copy()!!))
    havingRepositories(havingNsi = true)
    request = request.copy(requirementId = null, eventId = null)
    havingValidation()
    passesValidation()
  }

  @Test
  fun `Attempting to create contact with missing offender`() {
    havingDependentEntities()
    havingRepositories(havingOffender = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing type`() {
    havingDependentEntities()
    havingRepositories(havingType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing provider`() {
    havingDependentEntities()
    havingRepositories(havingProvider = false)
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing office location`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOfficeLocation = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing event`() {
    havingDependentEntities(havingEvent = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing requirement`() {
    havingDependentEntities(havingRequirement = false)
    havingRepositories()
    havingValidation()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid type`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid outcome type`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOutcomeType = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid office location`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidOfficeLocation = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with clashing appointment`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidFutureAppointment = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with invalid associated entity`() {
    havingDependentEntities()
    havingRepositories()
    havingValidation(havingValidAssociatedEntity = false)
    shouldThrowBadRequest()
  }

  private fun havingRepositories(
    havingOffender: Boolean = true,
    havingType: Boolean = true,
    havingProvider: Boolean = true,
    havingNsi: Boolean? = null,
  ) {
    request = Fake.newContact().copy(
      offenderCrn = offender.crn,
      nsiId = if (havingNsi == null) null else nsi.id,
      type = type.code,
      eventId = event.id,
      requirementId = requirement.id,
      outcome = outcome.code,
      officeLocation = officeLocation.code,
      staff = this.staff.code,
      team = team.code,
      provider = provider.code,
    )

    whenever(offenderRepository.findByCrn(offender.crn))
      .thenReturn(if (havingOffender) offender else null)
    whenever(contactTypeRepository.findSelectableByCode(type.code))
      .thenReturn(if (havingType) type else null)
    whenever(providerRepository.findByCodeAndSelectableIsTrue(provider.code))
      .thenReturn(if (havingProvider) provider else null)
    whenever(nsiRepository.findById(nsi.id))
      .thenReturn(if (havingNsi == true) Optional.of(nsi) else Optional.empty())
  }

  private fun havingValidation(
    havingValidType: Boolean = true,
    havingValidOutcomeType: Boolean = true,
    havingValidOfficeLocation: Boolean = true,
    havingValidFutureAppointment: Boolean = true,
    havingValidAssociatedEntity: Boolean = true,
  ) {
    if (!havingValidType) {
      whenever(validationService.validateContactType(request, type))
        .thenThrow(BadRequestException("bad request"))
    }

    val outcomeMock = whenever(validationService.validateOutcomeType(request, type))
    if (havingValidOutcomeType) {
      outcomeMock.thenReturn(outcome)
    } else {
      outcomeMock.thenThrow(BadRequestException("bad request"))
    }

    val officeLocationMock = whenever(validationService.validateOfficeLocation(request, type, team))
    if (havingValidOfficeLocation) {
      officeLocationMock.thenReturn(officeLocation)
    } else {
      officeLocationMock.thenThrow(BadRequestException("bad request"))
    }

    if (!havingValidFutureAppointment) {
      whenever(validationService.validateFutureAppointmentClashes(request, type, offender, null))
        .thenThrow(BadRequestException("bad request"))
    }

    if (!havingValidAssociatedEntity) {
      whenever(validationService.validateAssociatedEntity(type, requirement, event, null))
        .thenThrow(BadRequestException("bad request"))
    }
  }

  private fun shouldThrowBadRequest() {
    assertThrows<BadRequestException> {
      subject.createContact(request)
    }
  }

  private fun passesValidation() {
    whenever(contactRepository.saveAndFlush(any())).then(AdditionalAnswers.returnsFirstArg<Contact>())

    subject.createContact(request)

    verify(contactRepository).saveAndFlush(any())
  }

  private fun shouldSetAuditContext() {
    val context = AuditContext.get(AuditableInteraction.ADD_CONTACT)
    assertThat(context.offenderId).isEqualTo(offender.id)
  }
}
