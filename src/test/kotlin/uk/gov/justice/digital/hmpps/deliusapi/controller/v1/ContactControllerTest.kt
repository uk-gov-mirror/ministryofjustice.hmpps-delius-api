package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.service.ContactService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
class ContactControllerTest {
  @Mock
  private lateinit var service: ContactService

  @InjectMocks
  private lateinit var subject: ContactController

  @Test
  fun `Creating contact`() {
    val request = Fake.newContact()
    val contact = Fake.contact()

    whenever(service.createContact(request)).thenReturn(contact)

    val observed = subject.create(request)
    Assertions.assertThat(observed).isSameAs(contact)
  }
}
