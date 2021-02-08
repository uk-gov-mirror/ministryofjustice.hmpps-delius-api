package uk.gov.justice.digital.hmpps.deliusapi.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
class ContactServiceTest {

  @Mock
  private lateinit var repository: ContactRepository

  @InjectMocks
  private lateinit var subject: ContactService

  @Test
  fun `Creating contact`() {
    val newContact = Fake.newContact()
    val contact = Fake.contact()
    whenever(repository.saveAndFlush(any())).thenReturn(contact)

    val observed = subject.createContact(newContact)
    Assertions.assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .hasFieldOrPropertyWithValue("id", contact.id)
  }
}
