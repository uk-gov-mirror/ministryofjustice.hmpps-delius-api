package uk.gov.justice.digital.hmpps.deliusapi.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.Contact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
class ContactServiceTest {
  @InjectMocks
  private lateinit var subject: ContactService

  @Test
  fun `Creating contact`() {
    val newContact = Fake.newContact()
    val observed = subject.createContact(newContact)
    Assertions.assertThat(observed).isInstanceOf(Contact::class.java)
  }
}
