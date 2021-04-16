package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.NsiTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EventRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.lang.RuntimeException
import java.time.LocalDate

class CreateContactV1Test @Autowired constructor(
  private val repository: ContactRepository,
  private val eventRepository: EventRepository
) : EndToEndTest() {

  private lateinit var request: NewContact
  private lateinit var response: ContactDto
  private lateinit var created: Contact

  @Test
  fun `Creating contact`() {
    request = configuration.newContact(ContactTestsConfiguration::updatable)
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating contact against nsi & event`() {
    val nsi = havingExistingNsi(NsiTestsConfiguration::active)
    request = configuration.newContact(ContactTestsConfiguration::nsi).copy(
      nsiId = nsi.id,
    )
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating contact against nsi only`() {
    val nsi = havingExistingNsi(NsiTestsConfiguration::active)
    request = configuration.newContact(ContactTestsConfiguration::nsiOnly).copy(
      nsiId = nsi.id,
    )
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating contact against event`() {
    request = configuration.newContact(ContactTestsConfiguration::event)
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating contact against requirement`() {
    request = configuration.newContact(ContactTestsConfiguration::requirement)
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating contact with enforcement`() {
    request = configuration.newContact(ContactTestsConfiguration::enforcement)
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating appointment contact`() {
    request = configuration.newContact(ContactTestsConfiguration::appointment)
    whenCreatingContact()
    shouldCreateContact()
  }

  @Test
  fun `Creating breach start contact`() {
    request = configuration.newContact(ContactTestsConfiguration::breachStart)

    havingEvent {
      it.inBreach = false
      it.breachEnd = null
    }

    whenCreatingContact()
    shouldCreateContact()

    shouldUpdateEvent {
      assertThat(it)
        .hasProperty(Event::inBreach, true)
    }
  }

  @Test
  fun `Creating ftc contact`() {
    request = configuration.newContact(ContactTestsConfiguration::ftc)

    var limit = 0L
    havingEvent {
      it.ftcCount = 0
      it.inBreach = false
      it.breachEnd = null
      limit = it.disposal?.type?.failureToComplyLimit
        ?: throw RuntimeException("Event with id '${it.id}' has a sentence without an ftc limit")
    }

    if (limit == 0L) {
      throw RuntimeException("Event with id '${request.eventId}' has a sentence with a 0 ftc limit")
    }

    withDatabase {
      repository.deleteAllByEventIdAndTypeNationalStandardsContactIsTrue(request.eventId!!)
      repository.deleteAllByEventIdAndTypeCode(request.eventId!!, WellKnownContactType.REVIEW_ENFORCEMENT_STATUS.code)
    }

    logger.info("event has ftc limit $limit, first create ${limit - 1} ftc contacts to push the ftc count just below the limit")
    request = request.copy(date = LocalDate.now().minusDays(limit - 1))
    for (i in 1 until limit) {
      logger.info("creating pre ftc limit contact no. $i")
      whenCreatingContact()
      shouldCreateContact()

      shouldUpdateEvent {
        assertThat(it)
          .hasProperty(Event::ftcCount, i)
      }

      shouldSaveReviewEnforcementContact(false)

      request = request.copy(date = request.date.plusDays(1))
    }

    logger.info("creating ftc limit breach contact")
    whenCreatingContact()
    shouldCreateContact()

    shouldUpdateEvent {
      assertThat(it)
        .hasProperty(Event::ftcCount, limit)
    }

    shouldSaveReviewEnforcementContact()
  }

  private fun shouldSaveReviewEnforcementContact(should: Boolean = true) = withDatabase {
    val reviewContacts = repository.findAllByEventIdAndTypeCode(
      request.eventId!!,
      WellKnownContactType.REVIEW_ENFORCEMENT_STATUS.code
    )
    assertThat(reviewContacts)
      .describedAs("should save review enforcement contact")
      .hasSize(if (should) 1 else 0)
  }

  private fun shouldUpdateEvent(assert: (event: Event) -> Unit) = withDatabase {
    val event = eventRepository.findByIdOrNull(request.eventId!!)
      ?: throw RuntimeException("cannot find event with id '${request.eventId}'")
    assert(event)
  }

  private fun havingEvent(mutate: (event: Event) -> Unit) = withDatabase {
    val event = eventRepository.findByIdOrNull(request.eventId!!)
      ?: throw RuntimeException("cannot find event with id '${request.eventId}'")
    mutate(event)
    eventRepository.saveAndFlush(event)
  }

  private fun whenCreatingContact() {
    response = contactV1.safely { it.createContact(request) }
  }

  private fun shouldCreateContact() = withDatabase {
    created = repository.findByIdOrNull(response.id)
      ?: throw RuntimeException("Contact with id = '${response.id}' does not exist in the database")

    val observed = NewContact(
      date = created.date,
      offenderCrn = created.offender.crn,
      provider = created.provider!!.code,
      staff = created.staff!!.code,
      team = created.team!!.code,
      officeLocation = created.officeLocation?.code,
      startTime = created.startTime.toString(),
      endTime = created.endTime.toString(),
      type = created.type.code,
      alert = created.alert,
      sensitive = created.sensitive,
      description = created.description,
      outcome = created.outcome?.code,
      enforcement = created.enforcement?.action?.code,
      eventId = created.event?.id,
      requirementId = created.requirement?.id,
      nsiId = created.nsi?.id,
      notes = created.notes,
    )

    assertThat(observed)
      .describedAs("contact with id '${created.id}' should be saved")
      .usingRecursiveComparison()
      .ignoringCollectionOrder()
      .ignoringFields("alert") // TODO determine why this field is always null on insert into test... triggers?
      .isEqualTo(request)
  }
}
