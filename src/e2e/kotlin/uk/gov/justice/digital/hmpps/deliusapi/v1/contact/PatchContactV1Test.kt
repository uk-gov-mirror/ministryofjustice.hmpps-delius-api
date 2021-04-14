
package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.ApiException
import uk.gov.justice.digital.hmpps.deliusapi.client.model.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.ContactTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.deliusapi.config.newContact
import uk.gov.justice.digital.hmpps.deliusapi.util.Operation
import uk.gov.justice.digital.hmpps.deliusapi.util.assertThatException
import uk.gov.justice.digital.hmpps.deliusapi.util.extractingObject
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

class PatchContactV1Test : EndToEndTest() {
  private lateinit var contact: ContactDto
  private lateinit var response: ContactDto

  @Test
  fun `Attempting to patch non-editable contact`() {
    contact = havingExistingContact(ContactTestsConfiguration::notUpdatable)
    val exception = assertThrows<ApiException> {
      whenPatchingContact(Operation("replace", "/notes", "Updated note"))
    }
    assertThatException(exception)
      .hasProperty(ApiException::statusCode, 400)
      .extractingObject { it.error!! }
      .hasProperty(ErrorResponse::userMessage, "Contact type '${contact.type}' is not editable")
  }

  @Test
  fun `Patching contact notes`() {
    contact = havingExistingContact(ContactTestsConfiguration::updatable)
    whenPatchingContact(Operation("replace", "/notes", "Updated note"))
    assertThat(response)
      .hasProperty(ContactDto::notes, "${contact.notes}\n\n---------\n\nUpdated note")
  }

  @Test
  fun `Patching contact outcome`() {
    val newOutcome = configuration.newContact(ContactTestsConfiguration::updatable).outcome
      ?: throw RuntimeException("Bad test data, the updatable contact should have an outcome")
    contact = havingExistingContact(ContactTestsConfiguration::updatable) {
      it.copy(outcome = null) // create a contact without the outcome
    }
    // patch in the outcome
    whenPatchingContact(Operation("replace", "/outcome", newOutcome))

    assertThat(contact)
      .hasProperty(ContactDto::outcome, null)
    assertThat(response)
      .hasProperty(ContactDto::outcome, newOutcome)
  }

  private fun whenPatchingContact(vararg operations: Operation) {
    response = contactV1.safely { it.patchContact(contact.id, operations) }
  }
}
