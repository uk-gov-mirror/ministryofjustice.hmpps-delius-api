package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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
import uk.gov.justice.digital.hmpps.deliusapi.repository.models.LocalDateTimeWrapper
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStartDateTime
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class ContactBreachServiceTest {
  @Mock private lateinit var contactRepository: ContactRepository
  @Mock private lateinit var eventRepository: EventRepository
  @InjectMocks private lateinit var subject: ContactBreachService

  private lateinit var contact: Contact

  @Test
  fun `Attempting to update breach on insert without event`() {
    havingContact(WellKnownContactType.BREACH_INIT, havingEvent = false)
    whenUpdatingBreachOnInsertContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Attempting to update breach on insert with non-breach contact`() {
    havingContact(WellKnownContactType.COMMENCED)
    whenUpdatingBreachOnInsertContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Attempting to update breach on insert with breach start contact but breach already present in contact log`() {
    havingContact(WellKnownContactType.BREACH_INIT)
    havingLatestBreachEnd(contact.getStartDateTime().plusSeconds(1))
    whenUpdatingBreachOnInsertContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Attempting to update breach on insert with breach start contact but breach already present on event`() {
    havingContact(
      WellKnownContactType.BREACH_INIT,
      havingExistingEventBreach = LocalDate.of(2021, 3, 19)
    )
    havingLatestBreachEnd(null)
    whenUpdatingBreachOnInsertContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Updating breach on insert with breach start contact and no previous breach`() {
    havingContact(WellKnownContactType.BREACH_INIT)
    havingLatestBreachEnd(null)
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, true)
  }

  @Test
  fun `Updating breach on insert with breach start contact and expired breach in contact log`() {
    havingContact(WellKnownContactType.BREACH_INIT)
    havingLatestBreachEnd(LocalDateTime.of(2021, 3, 11, 13, 0))
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, true)
  }

  @Test
  fun `Attempting to update breach on insert with breach start contact and expired breach in contact log but future breach end date on event`() {
    havingContact(
      WellKnownContactType.BREACH_INIT,
      havingExistingEventBreach = LocalDate.of(2021, 3, 19)
    )
    havingLatestBreachEnd(LocalDateTime.of(2021, 3, 11, 13, 0))
    whenUpdatingBreachOnInsertContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Attempting to update breach on insert with breach start contact and expired breach end date on event but future breach end in contact log`() {
    havingContact(
      WellKnownContactType.BREACH_INIT,
      havingExistingEventBreach = LocalDate.of(2021, 3, 17)
    )
    havingLatestBreachEnd(LocalDateTime.of(2021, 3, 19, 13, 0))
    whenUpdatingBreachOnInsertContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Updating breach on insert with breach start contact and expired breach on event`() {
    havingContact(
      WellKnownContactType.BREACH_INIT,
      havingExistingEventBreach = LocalDate.of(2021, 3, 17)
    )
    havingLatestBreachEnd(null)
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, true)
  }

  @Test
  fun `Updating breach on insert with breach end contact and no existing breach`() {
    havingContact(WellKnownContactType.BREACH_CONCLUDED)
    havingLatestBreachStart(null)
    havingExistingFailureToComplyCountFromSentence()
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, false)
      .hasProperty(Event::breachEnd, contact.date)
      .hasProperty(Event::ftcCount, 2)
  }

  @Test
  fun `Updating breach on insert with breach end contact and existing breach start after contact date`() {
    havingContact(WellKnownContactType.BREACH_CONCLUDED)
    havingLatestBreachStart(contact.getStartDateTime().plusSeconds(1))
    havingExistingFailureToComplyCountFromSentence()
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, false)
      .hasProperty(Event::breachEnd, contact.date)
      .hasProperty(Event::ftcCount, 2)
  }

  @Test
  fun `Updating breach on insert with breach end contact and existing breach start before contact date`() {
    havingContact(WellKnownContactType.BREACH_CONCLUDED)
    havingLatestBreachStart(contact.getStartDateTime().minusSeconds(1))
    havingExistingFailureToComplyCountFromSentence()
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, true)
      .hasProperty(Event::breachEnd, contact.date)
      .hasProperty(Event::ftcCount, 2)
  }

  @Test
  fun `Updating breach on insert with prison recall contact and existing breach start on same day as contact date`() {
    havingContact(WellKnownContactType.BREACH_PRISON_RECALL)
    havingLatestBreachStart(contact.getStartDateTime().minusSeconds(1))
    havingExistingFailureToComplyCountFromSentence()
    whenUpdatingBreachOnInsertContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, false)
      .hasProperty(Event::breachEnd, contact.date)
      .hasProperty(Event::ftcCount, 2)
  }

  @Test
  fun `Attempting to update breach on update without event`() {
    havingContact(WellKnownContactType.BREACH_INIT, havingEvent = false)
    whenUpdatingBreachOnUpdateContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Attempting to update breach on update with non-breach contact`() {
    havingContact(WellKnownContactType.COMMENCED)
    whenUpdatingBreachOnUpdateContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Updating breach on update but no new or existing breach`() {
    havingContact(WellKnownContactType.BREACH_INIT)
    havingLatestBreachStart(null)
    whenUpdatingBreachOnUpdateContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Updating breach on update and no changes`() {
    havingContact(
      WellKnownContactType.BREACH_INIT,
      havingExistingEventBreach = LocalDate.of(2021, 4, 18)
    )
    havingLatestBreachStart(LocalDateTime.of(2021, 3, 18, 0, 0))
    havingLatestBreachEnd(LocalDateTime.of(2021, 4, 18, 0, 0))
    whenUpdatingBreachOnUpdateContact()
    shouldNotUpdateEvent()
  }

  @Test
  fun `Updating breach on update and adding breach`() {
    havingContact(WellKnownContactType.BREACH_INIT)
    havingLatestBreachStart(LocalDateTime.of(2021, 3, 18, 0, 0))
    havingLatestBreachEnd(LocalDateTime.of(2021, 4, 18, 0, 0))
    havingExistingFailureToComplyCountFromSentence()
    whenUpdatingBreachOnUpdateContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, true)
      .hasProperty(Event::breachEnd, LocalDate.of(2021, 4, 18))
      .hasProperty(Event::ftcCount, 2)
  }

  @Test
  fun `Updating breach on update and removing breach`() {
    havingContact(
      WellKnownContactType.BREACH_INIT,
      havingExistingEventBreach = LocalDate.of(2021, 4, 18)
    )
    havingLatestBreachStart(null)
    havingExistingFailureToComplyCountFromSentence()
    whenUpdatingBreachOnUpdateContact()
    shouldUpdateEvent()
    assertThat(contact.event!!)
      .hasProperty(Event::inBreach, false)
      .hasProperty(Event::breachEnd, null)
      .hasProperty(Event::ftcCount, 2)
  }

  private fun havingContact(
    havingContactType: WellKnownContactType,
    havingEvent: Boolean = true,
    havingExistingEventBreach: LocalDate? = null,
  ) {
    contact = Fake.contact().apply {
      if (!havingEvent) event = null
      date = LocalDate.of(2021, 3, 18)
      startTime = LocalTime.of(13, 0)
      endTime = null
      type.code = havingContactType.code
      event?.apply {
        breachEnd = havingExistingEventBreach
        inBreach = havingExistingEventBreach != null
        disposals?.get(0)?.date = LocalDate.of(2021, 2, 1)
      }
    }
  }

  private fun havingLatestBreachStart(value: LocalDateTime?) {
    val result = if (value == null) emptyList()
    else listOf(LocalDateTimeWrapper(value.toLocalDate(), value.toLocalTime()))
    whenever(contactRepository.findAllBreachDates(contact.event!!.id, WellKnownContactType.BREACH_START_CODES))
      .thenReturn(result)
  }

  private fun havingLatestBreachEnd(value: LocalDateTime?) {
    val result = if (value == null) emptyList()
    else listOf(LocalDateTimeWrapper(value.toLocalDate(), value.toLocalTime()))
    whenever(contactRepository.findAllBreachDates(contact.event!!.id, WellKnownContactType.BREACH_END_CODES))
      .thenReturn(result)
  }

  private fun havingExistingFailureToComplyCountFromSentence() {
    val event = contact.event!!
    whenever(contactRepository.countFailureToComply(eq(event.id), any()))
      .thenReturn(2)
  }

  private fun whenUpdatingBreachOnInsertContact() = subject.updateBreachOnInsertContact(contact)

  private fun whenUpdatingBreachOnUpdateContact() = subject.updateBreachOnUpdateContact(contact)

  private fun shouldNotUpdateEvent() = verifyNoMoreInteractions(eventRepository)

  private fun shouldUpdateEvent() = verify(eventRepository, times(1)).saveAndFlush(contact.event)
}
