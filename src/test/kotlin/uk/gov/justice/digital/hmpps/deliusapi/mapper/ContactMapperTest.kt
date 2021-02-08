package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class ContactMapperTest {

  @Test
  fun `Mapping from request to dto`() {
    val source = Fake.newContact()
    val observed = ContactMapper.INSTANCE.toDto(source)

    Assertions.assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .usingRecursiveComparison().ignoringFields("id").isEqualTo(source)
  }

  @Test
  fun `Mapping from entity to dto`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toDto(source)

    Assertions.assertThat(observed)
      .isInstanceOf(ContactDto::class.java)
      .usingRecursiveComparison().ignoringFields(
        "offenderId",
        "contactType",
        "contactOutcome",
        "provider",
        "team",
        "staff",
        "officeLocation",
        "contactShortDescription"
      ).isEqualTo(source)
    Assertions.assertThat(observed.offenderId).isEqualTo(source.offender?.id)
    Assertions.assertThat(observed.contactType).isEqualTo(source.contactType?.code)
    Assertions.assertThat(observed.contactOutcome).isEqualTo(source.contactOutcomeType?.code)
    Assertions.assertThat(observed.provider).isEqualTo(source.provider?.code)
    Assertions.assertThat(observed.team).isEqualTo(source.team?.code)
    Assertions.assertThat(observed.staff).isEqualTo(source.staff?.code)
    Assertions.assertThat(observed.officeLocation).isEqualTo(source.officeLocation?.code)
  }
}
