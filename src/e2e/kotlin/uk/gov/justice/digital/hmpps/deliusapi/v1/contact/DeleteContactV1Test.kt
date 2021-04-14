package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.ApiException
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.deliusapi.util.assertThatException
import uk.gov.justice.digital.hmpps.deliusapi.util.extractingObject
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

class DeleteContactV1Test : EndToEndTest() {
  private lateinit var contact: ContactDto

  @Test
  fun `Attempting to delete non-editable contact`() {
    contact = havingExistingContact(ContactTestsConfiguration::notUpdatable)
    val exception = assertThrows<ApiException> { whenDeletingContact() }
    assertThatException(exception)
      .hasProperty(ApiException::statusCode, 400)
      .extractingObject { it.error!! }
      .hasProperty(
        ErrorResponse::userMessage,
        "Contact with id '${contact.id}' cannot be deleted, contact type '${contact.type}' is not editable"
      )
  }

  @Test
  fun `Successfully deleting contact`() {
    contact = havingExistingContact(ContactTestsConfiguration::updatable)
    assertDoesNotThrow { whenDeletingContact() }
  }

  private fun whenDeletingContact() {
    contactV1.safely { it.deleteContact(contact.id) }
  }
}
