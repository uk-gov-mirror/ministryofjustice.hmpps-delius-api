package uk.gov.justice.digital.hmpps.deliusapi.v1.contact

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
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import java.lang.RuntimeException

class CreateContactV1Test @Autowired constructor(
  private val repository: ContactRepository
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

  private fun whenCreatingContact() {
    response = contactV1.safely { it.createContact(request) }
  }

  private fun shouldCreateContact() {
    if (!databaseAssertEnabled()) {
      return
    }

    created = repository.findByIdOrNull(response.id)
      ?: throw RuntimeException("Contact with id = '${response.id}' does not exist in the database")

    TODO("implement assertions against saved contact")
  }
}
