package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ContactServiceTest {

  @Mock
  private lateinit var contactRepository: ContactRepository

  @Mock
  private lateinit var offenderRepository: OffenderRepository

  @InjectMocks
  private lateinit var subject: ContactService

  @Test
  fun `Creating contact`() {
    val newContact = Fake.newContact()
    val contact = Fake.contact()
    val offender = contact.offender

    whenever(offenderRepository.findByCrn(newContact.offenderCrn)).thenReturn(Optional.of(offender))
    whenever(contactRepository.saveAndFlush(any())).thenReturn(contact)

    val observed = subject.createContact(newContact)
    Assertions.assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .hasFieldOrPropertyWithValue("id", contact.id)
  }

  @Test
  fun `Attempting to create contact with missing offender`() {
    val newContact = Fake.newContact()

    whenever(offenderRepository.findByCrn(newContact.offenderCrn)).thenReturn(Optional.empty())

    assertThrows<BadRequestException> { subject.createContact(newContact) }
  }
}
