package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EventRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ContactEnforcementServiceTest {
  @Mock private lateinit var contactRepository: ContactRepository
  @Mock private lateinit var eventRepository: EventRepository
  @Mock private lateinit var systemContactService: SystemContactService
  @Mock private lateinit var contactBreachService: ContactBreachService
  @InjectMocks private lateinit var subject: ContactEnforcementService

  private val enforcementContact = Fake.contact()
  private lateinit var contact: Contact
  private val newFtcCount = Fake.count()

  @Test
  fun `Updating failure to comply without event`() {
    havingContact(havingEvent = false)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply with non-national standards contact`() {
    havingContact(havingNationalStandardsContact = false)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply without outcome but ftc count not changed`() {
    havingContact(havingOutcome = false)
    havingCurrentFtcCountChanged(false)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply without outcome and ftc count changed`() {
    havingContact(havingOutcome = false)
    havingCurrentFtcCountChanged()
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount()
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply without disposal and ftc count not changed`() {
    havingContact(havingDisposal = false)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply but compliant acceptable`() {
    havingContact(havingCompliantAcceptable = true)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply and ftc count changed but enforcement already initiates a breach`() {
    havingContact(havingEnforcementContactType = WellKnownContactType.BREACH_INIT)
    havingCurrentFtcCountChanged()
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(true)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply but event already in breach by flag`() {
    havingContact(havingEventAlreadyInBreachByFlag = true)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply but event already in breach by date`() {
    havingContact(havingEventAlreadyInBreachByDate = true)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply but event has no sentence`() {
    havingContact(havingSentenceType = false)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply but ftc limit not breached`() {
    havingContact(havingFtcLimitBreached = false)
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply but enforcement under review`() {
    havingContact()
    havingEnforcementUnderReview()
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(false)
  }

  @Test
  fun `Updating failure to comply`() {
    havingContact()
    havingEnforcementUnderReview(false)
    havingEnforcementContact()
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(false)
    shouldUpdateBreach(true)
  }

  @Test
  fun `Updating failure to comply for release from custody contact`() {
    havingContact(havingContactType = WellKnownContactType.RELEASE_FROM_CUSTODY, havingNationalStandardsContact = false)
    havingCurrentFtcCountChanged()
    whenUpdatingFailureToComply()
    shouldUpdateFtcCount(true)
    shouldUpdateBreach(false)
  }

  private fun havingContact(
    havingContactType: WellKnownContactType? = null,
    havingEvent: Boolean = true,
    havingEventAlreadyInBreachByFlag: Boolean = false,
    havingEventAlreadyInBreachByDate: Boolean = false,
    havingOutcome: Boolean = true,
    havingActionRequired: Boolean = true,
    havingCompliantAcceptable: Boolean = false,
    havingDisposal: Boolean = true,
    havingSentenceType: Boolean = true,
    havingNationalStandardsContact: Boolean = true,
    havingEnforcementContactType: WellKnownContactType = WellKnownContactType.BREACH_CONCLUDED,
    havingFtcLimitBreached: Boolean = true,
  ) {
    contact = Fake.contact().apply {
      date = LocalDate.of(2021, 3, 18)
      type.apply {
        nationalStandardsContact = havingNationalStandardsContact
        if (havingContactType != null) {
          code = havingContactType.code
        }
      }
      if (!havingEvent) event = null
      if (!havingOutcome) outcome = null
      event?.apply {
        if (!havingDisposal) disposal = null
        inBreach = havingEventAlreadyInBreachByFlag
        breachEnd = if (havingEventAlreadyInBreachByDate) LocalDate.of(2021, 3, 19) else null
        ftcCount = if (havingFtcLimitBreached) 100 else 99
        disposal?.apply {
          if (!havingSentenceType) type?.sentenceType = null
          type?.failureToComplyLimit = 100
        }
      }
      outcome?.apply {
        actionRequired = havingActionRequired
        compliantAcceptable = havingCompliantAcceptable
      }
      enforcement?.apply {
        action?.contactType?.code = havingEnforcementContactType.code
      }
    }
  }

  private fun havingCurrentFtcCountChanged(having: Boolean = true) {
    val event = contact.event!!
    val count = if (having) newFtcCount else event.ftcCount
    whenever(contactRepository.countFailureToComply(eq(event.id), any()))
      .thenReturn(count)
  }

  private fun havingEnforcementUnderReview(having: Boolean = true) {
    val event = contact.event!!
    whenever(contactRepository.countEnforcementUnderReview(event.id, WellKnownContactType.REVIEW_ENFORCEMENT_STATUS.code, event.breachEnd))
      .thenReturn(if (having) 1 else 0)
  }

  private fun havingEnforcementContact() {
    whenever(systemContactService.createLinkedSystemContact(contact, WellKnownContactType.REVIEW_ENFORCEMENT_STATUS))
      .thenReturn(enforcementContact)
  }

  private fun whenUpdatingFailureToComply() = subject.updateFailureToComply(contact)

  private fun shouldUpdateFtcCount(should: Boolean = true) {
    if (should) {
      verify(eventRepository, times(1)).saveAndFlush(contact.event)
      assertThat(contact.event!!)
        .hasProperty(Event::ftcCount, newFtcCount)
    } else verify(eventRepository, never()).saveAndFlush(any())
  }

  private fun shouldUpdateBreach(should: Boolean = true) {
    if (should) verify(contactBreachService, times(1)).updateBreachOnInsertContact(enforcementContact)
    else verify(contactBreachService, never()).updateBreachOnInsertContact(any())
  }
}
