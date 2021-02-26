package uk.gov.justice.digital.hmpps.deliusapi.service

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.ContactService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
abstract class ContactServiceTestBase {
  @Mock protected lateinit var contactRepository: ContactRepository
  @Mock protected lateinit var offenderRepository: OffenderRepository
  @Mock protected lateinit var contactTypeRepository: ContactTypeRepository
  @Mock protected lateinit var providerRepository: ProviderRepository
  @InjectMocks protected lateinit var subject: ContactService

  protected lateinit var type: ContactType
  protected lateinit var outcome: ContactOutcomeType
  protected lateinit var offender: Offender
  protected lateinit var provider: Provider
  protected lateinit var staff: Staff
  protected lateinit var team: Team
  protected lateinit var officeLocation: OfficeLocation
  protected lateinit var event: Event
  protected lateinit var requirement: Requirement

  protected fun havingDependentEntities(
    havingEvent: Boolean = true,
    havingRequirement: Boolean = true,
    havingOutcome: Boolean = true,
    havingOfficeLocation: Boolean = true,
    havingTeam: Boolean = true,
    havingStaff: Boolean = true,
  ) {
    val offenderId = Fake.id()

    requirement = Fake.requirement().copy(offenderId = offenderId)
    val requirements = if (havingRequirement) listOf(requirement, Fake.requirement()) else listOf()
    val disposals = listOf(Fake.disposal().copy(requirements = requirements))
    event = Fake.event().copy(disposals = disposals)
    val events = if (havingEvent) listOf(event, Fake.event()) else listOf()

    offender = Fake.offender().copy(id = offenderId, events = events)

    outcome = Fake.contactOutcomeType()
    type = Fake.contactType().copy(
      outcomeTypes = if (havingOutcome) listOf(outcome, Fake.contactOutcomeType()) else listOf()
    )

    this.staff = Fake.staff()
    val staff = if (havingStaff) listOf(this.staff, Fake.staff()) else listOf()

    officeLocation = Fake.officeLocation()
    val officeLocations = if (havingOfficeLocation) listOf(officeLocation, Fake.officeLocation()) else listOf()

    team = Fake.team().copy(staff = staff, officeLocations = officeLocations)
    val teams = if (havingTeam) listOf(team, Fake.team()) else listOf()

    provider = Fake.provider().copy(teams = teams)
  }
}
