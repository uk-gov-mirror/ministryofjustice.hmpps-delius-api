package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.service.ContactServiceTestBase
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.lang.IllegalArgumentException
import java.util.Optional

class CreateSystemContactTest : ContactServiceTestBase() {
  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<Contact>
  private lateinit var request: NewSystemContact

  @Test
  fun `Successfully creating system contact with type by id`() {
    havingDependentEntities()
    havingRepositories()
    whenCreatingSystemContactShouldSuccessfullySaveContact()
  }

  @Test
  fun `Successfully creating system contact with type by code`() {
    havingDependentEntities()
    havingRepositories(havingTypeById = null, havingTypeByCode = true)
    whenCreatingSystemContactShouldSuccessfullySaveContact()
  }

  @Test
  fun `Attempting to create system contact with missing type by id`() {
    havingDependentEntities()
    havingRepositories(havingTypeById = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing type by code`() {
    havingDependentEntities()
    havingRepositories(havingTypeById = null, havingTypeByCode = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing offender`() {
    havingDependentEntities()
    havingRepositories(havingOffender = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing provider`() {
    havingDependentEntities()
    havingRepositories(havingProvider = false)
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing team`() {
    havingDependentEntities(havingTeam = false)
    havingRepositories()
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing staff`() {
    havingDependentEntities(havingStaff = false)
    havingRepositories()
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  @Test
  fun `Attempting to create system contact with missing event`() {
    havingDependentEntities(havingEvent = false)
    havingRepositories()
    whenCreatingSystemContactShouldThrowIllegalArgumentException()
  }

  private fun whenCreatingSystemContactShouldSuccessfullySaveContact() {
    val savedContact = Fake.contact()

    whenever(contactRepository.saveAndFlush(entityCaptor.capture()))
      .thenReturn(savedContact)

    subject.createSystemContact(request)

    assertThat(entityCaptor.value)
      .hasProperty(Contact::offender, offender)
      .hasProperty(Contact::type, type)
      .hasProperty(Contact::outcome, null)
      .hasProperty(Contact::provider, provider)
      .hasProperty(Contact::team, team)
      .hasProperty(Contact::staff, staff)
      .hasProperty(Contact::event, event)
      .hasProperty(Contact::requirement, null)
      .hasProperty(Contact::officeLocation, null)
      .hasProperty(Contact::date, request.timestamp.toLocalDate())
      .hasProperty(Contact::startTime, request.timestamp.toLocalTime())
      .hasProperty(Contact::endTime, null)
      .hasProperty(Contact::alert, false)
      .hasProperty(Contact::sensitive, false)
      .hasProperty(Contact::notes, request.notes)
      .hasProperty(Contact::description, null)
      .hasProperty(Contact::partitionAreaId, 0)
      .hasProperty(Contact::staffEmployeeId, 1)
      .hasProperty(Contact::teamProviderId, 1)
  }

  private fun havingRepositories(
    havingOffender: Boolean = true,
    havingTypeById: Boolean? = true,
    havingTypeByCode: Boolean? = null,
    havingProvider: Boolean = true,
  ) {
    request = Fake.newSystemContact().copy(
      typeId = if (havingTypeById == null) null else type.id,
      type = if (havingTypeByCode == null) null
      else Fake.faker.options().option(WellKnownContactType::class.java),
      offenderId = offender.id,
      eventId = event.id,
      providerId = provider.id,
      teamId = team.id,
      staffId = staff.id,
    )

    whenever(offenderRepository.findById(offender.id))
      .thenReturn(if (havingOffender) Optional.of(offender) else Optional.empty())

    when (havingTypeById) {
      true -> whenever(contactTypeRepository.findById(type.id)).thenReturn(Optional.of(type))
      false -> whenever(contactTypeRepository.findById(type.id)).thenReturn(Optional.empty())
    }

    when (havingTypeByCode) {
      true -> whenever(contactTypeRepository.findByCode(request.type?.code!!)).thenReturn(type)
      false -> whenever(contactTypeRepository.findByCode(request.type?.code!!)).thenReturn(null)
    }

    whenever(providerRepository.findById(provider.id))
      .thenReturn(if (havingProvider) Optional.of(provider) else Optional.empty())
  }

  private fun whenCreatingSystemContactShouldThrowIllegalArgumentException() {
    assertThrows<IllegalArgumentException> {
      subject.createSystemContact(request)
    }
  }
}
