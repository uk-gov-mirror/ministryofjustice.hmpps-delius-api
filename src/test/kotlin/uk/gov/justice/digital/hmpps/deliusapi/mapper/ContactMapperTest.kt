package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class ContactMapperTest {

  @Test
  fun `Mapping from request to entity`() {
    val request = Fake.newContact()
    val entity = ContactMapper.INSTANCE.toEntity(request)
    Assertions.assertThat(entity).isInstanceOf(Contact::class.java)
  }
}
