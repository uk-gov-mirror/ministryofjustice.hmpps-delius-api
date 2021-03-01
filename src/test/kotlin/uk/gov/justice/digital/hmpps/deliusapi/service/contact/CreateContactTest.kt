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
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth
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

    havingDependentEntities()
    havingRepositories()

    whenever(contactRepository.saveAndFlush(entityCaptor.capture()))
      .thenReturn(savedContact)

    val observed = subject.createContact(request)
    assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .hasFieldOrPropertyWithValue("id", savedContact.id)

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
      .hasProperty(Contact::notes, request.notes)
      .hasProperty(Contact::description, request.description)
      .hasProperty(Contact::partitionAreaId, 0)
      .hasProperty(Contact::staffEmployeeId, 1)
      .hasProperty(Contact::teamProviderId, 1)

    shouldSetAuditContext()
  }

  @Test
  fun `Creating future contact with acceptable absence outcome`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(date = Fake.randomFutureLocalDate())

    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(outcomeTypes = listOf(outcome.copy(attendance = false, compliantAcceptable = true))))

    passesValidation()
  }

  @Test
  fun `Creating future contact with no outcome`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(date = Fake.randomFutureLocalDate(), outcome = null)

    passesValidation()
  }

  @Test
  fun `Creating historic contact with no outcome when outcome not required`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(date = Fake.randomPastLocalDate(), outcome = null)

    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(outcomeFlag = YesNoBoth.N))

    passesValidation()
  }

  @Test
  fun `Creating contact with no location when location not required`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(officeLocation = null)

    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(locationFlag = YesNoBoth.N))

    passesValidation()
  }

  @Test
  fun `Creating contact with no location when location optional`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(officeLocation = null)

    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(locationFlag = YesNoBoth.B))

    passesValidation()
  }

  @Test
  fun `Creating contact with location when location optional`() {
    havingDependentEntities()
    havingRepositories()
    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(locationFlag = YesNoBoth.B))

    passesValidation()
  }

  @Test
  fun `Creating non-attendance and non-recordedHoursCredited contact without times`() {
    havingDependentEntities()
    havingRepositories()

    request = request.copy(startTime = null, endTime = null)

    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(attendanceContact = false, recordedHoursCredited = false))

    passesValidation()
  }

  @Test
  fun `Creating outcomed non-recordedHoursCredited contact without end time`() {
    havingDependentEntities()
    havingRepositories()

    request = request.copy(endTime = null)

    whenever(contactTypeRepository.findSelectableByCode(request.type))
      .thenReturn(type.copy(recordedHoursCredited = false))

    passesValidation()
  }

  @Test
  fun `Attempting to create outcomed recordedHoursCredited contact without end time`() {
    havingDependentEntities()
    havingRepositories()

    request = request.copy(endTime = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create attendance contact without start time`() {
    havingDependentEntities()
    havingRepositories()

    request = request.copy(startTime = null, endTime = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to creating contact with no location when location required`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(officeLocation = null)

    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create historic contact with no outcome when outcome required`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(date = Fake.randomPastLocalDate(), outcome = null)

    shouldThrowBadRequest()
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
  fun `Attempting to create historic contact with missing outcome`() {
    havingDependentEntities(havingOutcome = false)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing provider`() {
    havingDependentEntities()
    havingRepositories(havingProvider = false)
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing office location`() {
    havingDependentEntities(havingOfficeLocation = false)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing event`() {
    havingDependentEntities(havingEvent = false)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with missing requirement`() {
    havingDependentEntities(havingRequirement = false)
    havingRepositories()
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with type not matching outcome`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(outcome = "INVALID")
    shouldThrowBadRequest()
  }

  @Test
  fun `Attempting to create contact with date in past and no contact outcome`() {
    havingDependentEntities()
    havingRepositories()
    request = request.copy(date = Fake.randomPastLocalDate(), outcome = null)
    shouldThrowBadRequest()
  }

  private fun havingRepositories(
    havingOffender: Boolean = true,
    havingType: Boolean = true,
    havingProvider: Boolean = true,
    havingNsi: Boolean = true,
  ) {
    request = Fake.newContact().copy(
      offenderCrn = offender.crn,
      nsiId = nsi.id,
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
      .thenReturn(if (havingNsi) Optional.of(nsi) else Optional.empty())
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
