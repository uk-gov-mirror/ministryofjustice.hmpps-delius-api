package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.util.Optional

class GetUpdateContactTest : ContactServiceTestBase() {
  @Test
  fun `Successfully getting update contact`() {
    val contact = Fake.contact()
    val update = Fake.updateContact()

    whenever(contactRepository.findById(contact.id)).thenReturn(Optional.of(contact))
    whenever(mapper.toUpdate(contact)).thenReturn(update)

    val observed = subject.getUpdateContact(contact.id)

    assertThat(observed).isSameAs(update)
  }

  @Test
  fun `Attempting to get missing update contact`() {
    whenever(contactRepository.findById(100)).thenReturn(Optional.empty())
    assertThrows<NotFoundException> { subject.getUpdateContact(100) }
  }
}
