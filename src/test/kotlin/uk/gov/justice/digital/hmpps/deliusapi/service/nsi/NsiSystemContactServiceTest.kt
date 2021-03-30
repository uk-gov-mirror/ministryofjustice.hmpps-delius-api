package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.NewSystemContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.SystemContactService
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class NsiSystemContactServiceTest {
  @Mock private lateinit var contactRepository: ContactRepository
  @Mock private lateinit var systemContactService: SystemContactService
  @Captor private lateinit var captor: ArgumentCaptor<NewSystemContact>
  @InjectMocks private lateinit var subject: NsiSystemContactService

  @Test
  fun `Creating referral contact`() {
    val nsi = Fake.nsi()
    havingCreateSystemContactCaptor()
    subject.createReferralContact(nsi)
    shouldCreateWellKnownContact(nsi, WellKnownContactType.NSI_REFERRAL, nsi.referralDate)
  }

  @Test
  fun `Creating status contact`() {
    val nsi = Fake.nsi()
    havingCreateSystemContactCaptor()
    subject.createStatusContact(nsi)
    shouldCreateStatusContact(nsi, nsi.status!!, nsi.statusDate)
  }

  @Test
  fun `Updating status contact with new status`() {
    val nsi = Fake.nsi()
    val newStatus = Fake.nsiStatus()
    val request = Fake.updateNsi()
    havingCreateSystemContactCaptor()
    subject.updateStatusContact(nsi, newStatus, request)
    shouldCreateStatusContact(nsi, newStatus, request.statusDate)
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating status contact but status not changed`() {
    val nsi = Fake.nsi()
    val request = Fake.updateNsi()
    subject.updateStatusContact(nsi, nsi.status!!, request)
    shouldNotCreateAnySystemContacts()
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating status contact and status not changed but status date changed`() {
    val nsi = Fake.nsi()
    val newStatus = Fake.nsiStatus().apply { id = nsi.status!!.id }
    val request = Fake.updateNsi().copy(statusDate = nsi.statusDate.plusMinutes(1))

    val existing = Fake.contact()
    whenever(
      contactRepository.findAllByNsiIdAndTypeIdAndDate(
        nsi.id, newStatus.contactTypeId, nsi.statusDate.toLocalDate()
      )
    )
      .thenReturn(listOf(existing))

    subject.updateStatusContact(nsi, newStatus, request)

    shouldNotCreateAnySystemContacts()
    shouldUpdateContact(existing)

    // should update existing status date
    assertThat(existing)
      .hasProperty(Contact::date, request.statusDate.toLocalDate())
      .hasProperty(Contact::startTime, request.statusDate.toLocalTime())
  }

  @Test
  fun `Creating commencement contact but nsi has nbo start date`() {
    val nsi = Fake.nsi().apply { startDate = null }
    subject.createCommencedContact(nsi)
    shouldNotCreateAnySystemContacts()
  }

  @Test
  fun `Creating commencement contact`() {
    val nsi = Fake.nsi().apply { startDate = LocalDate.of(2021, 3, 26) }
    havingCreateSystemContactCaptor()
    subject.createCommencedContact(nsi)
    shouldCreateWellKnownContact(nsi, WellKnownContactType.NSI_COMMENCED, nsi.startDate!!)
  }

  @Test
  fun `Updating commencement contact when nsi is not yet commenced`() {
    val nsi = Fake.nsi().apply { startDate = null }
    val request = Fake.updateNsi().copy(startDate = LocalDate.of(2021, 3, 26))
    havingCreateSystemContactCaptor()
    subject.updateCommencedContact(nsi, request)
    shouldCreateWellKnownContact(nsi, WellKnownContactType.NSI_COMMENCED, request.startDate!!)
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating commencement contact when commencement date not changed`() {
    val nsi = Fake.nsi().apply { startDate = LocalDate.of(2021, 3, 26) }
    val request = Fake.updateNsi().copy(startDate = LocalDate.of(2021, 3, 26))
    subject.updateCommencedContact(nsi, request)
    shouldNotCreateAnySystemContacts()
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating commencement contact when commencement date changed`() {
    val nsi = Fake.nsi().apply { startDate = LocalDate.of(2021, 3, 26) }
    val request = Fake.updateNsi().copy(startDate = LocalDate.of(2021, 3, 27))
    val existing = havingContactByNsiAndWellKnownType(nsi, WellKnownContactType.NSI_COMMENCED)

    subject.updateCommencedContact(nsi, request)

    shouldNotCreateAnySystemContacts()
    shouldUpdateContact(existing)
    assertThat(existing)
      .hasProperty(Contact::date, request.startDate!!)
  }

  @Test
  fun `Creating termination contact but nsi has no outcome`() {
    val nsi = Fake.nsi().apply { outcome = null }
    subject.createTerminationContact(nsi)
    shouldNotCreateAnySystemContacts()
  }

  @Test
  fun `Creating termination contact`() {
    val nsi = Fake.nsi()
    havingCreateSystemContactCaptor()
    subject.createTerminationContact(nsi)
    shouldCreateTerminatedContact(nsi, nsi.endDate!!, nsi.outcome!!)
  }

  @Test
  fun `Updating termination contact but nsi has no outcome & no outcome set`() {
    val nsi = Fake.nsi().apply { outcome = null }
    val request = Fake.updateNsi()
    subject.updateTerminationContact(nsi, null, request)
    shouldNotCreateAnySystemContacts()
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating termination contact but outcome not changed`() {
    val nsi = Fake.nsi()
    val request = Fake.updateNsi()
    subject.updateTerminationContact(nsi, nsi.outcome!!, request)
    shouldNotCreateAnySystemContacts()
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating termination contact and setting new outcome`() {
    val nsi = Fake.nsi().apply { outcome = null }
    val request = Fake.updateNsi()
    val newOutcome = Fake.standardReference()
    havingCreateSystemContactCaptor()
    subject.updateTerminationContact(nsi, newOutcome, request)
    shouldCreateTerminatedContact(nsi, request.endDate!!, newOutcome)
    shouldNotUpdateAnyContacts()
  }

  @Test
  fun `Updating termination contact and updating existing outcome`() {
    val nsi = Fake.nsi()
    val request = Fake.updateNsi()
    val newOutcome = Fake.standardReference()
    val existing = havingContactByNsiAndWellKnownType(nsi, WellKnownContactType.NSI_TERMINATED)

    subject.updateTerminationContact(nsi, newOutcome, request)
    shouldNotCreateAnySystemContacts()
    shouldUpdateContact(existing)

    assertThat(existing)
      .hasProperty(Contact::date, request.endDate!!)
      .hasProperty(Contact::notes, "NSI Terminated with Outcome: ${newOutcome.description}")
  }

  private fun shouldCreateTerminatedContact(nsi: Nsi, date: LocalDate, newOutcome: StandardReference) {
    shouldCreateWellKnownContact(
      nsi, WellKnownContactType.NSI_TERMINATED, date,
      notes = "NSI Terminated with Outcome: ${newOutcome.description}"
    )
  }

  private fun havingCreateSystemContactCaptor() = doNothing()
    .whenever(systemContactService).createSystemContact(capture(captor))

  private fun havingContactByNsiAndWellKnownType(nsi: Nsi, type: WellKnownContactType): Contact {
    val existing = Fake.contact()
    whenever(contactRepository.findAllByNsiIdAndTypeCode(nsi.id, type.code))
      .thenReturn(listOf(existing))
    return existing
  }

  private fun shouldCreateWellKnownContact(
    nsi: Nsi,
    type: WellKnownContactType,
    date: LocalDate,
    startTime: LocalTime? = null,
    notes: String? = null
  ) =
    assertThat(captor.value)
      .usingRecursiveComparison()
      .isEqualTo(
        NewSystemContact(
          type = type,
          offenderId = nsi.offender?.id!!,
          nsiId = nsi.id,
          eventId = nsi.event?.id,
          providerId = nsi.manager?.provider?.id!!,
          teamId = nsi.manager?.team?.id!!,
          staffId = nsi.manager?.staff?.id!!,
          date = date,
          startTime = startTime,
          notes = notes,
        )
      )

  private fun shouldCreateStatusContact(nsi: Nsi, status: NsiStatus, date: LocalDateTime) =
    assertThat(captor.value)
      .usingRecursiveComparison()
      .isEqualTo(
        NewSystemContact(
          typeId = status.contactTypeId,
          offenderId = nsi.offender?.id!!,
          nsiId = nsi.id,
          eventId = nsi.event?.id,
          providerId = nsi.manager?.provider?.id!!,
          teamId = nsi.manager?.team?.id!!,
          staffId = nsi.manager?.staff?.id!!,
          date = date.toLocalDate(),
          startTime = date.toLocalTime(),
        )
      )

  private fun shouldNotCreateAnySystemContacts() = verify(systemContactService, never()).createSystemContact(any())

  private fun shouldNotUpdateAnyContacts() = verify(contactRepository, never()).saveAndFlush(any())

  private fun shouldUpdateContact(contact: Contact) =
    verify(contactRepository, times(1)).saveAndFlush(contact)
}
