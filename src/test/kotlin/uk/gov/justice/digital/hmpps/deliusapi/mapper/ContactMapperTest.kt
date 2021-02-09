package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class ContactMapperTest {

  @Test
  fun `Mapping from entity to dto`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toDto(source)

    Assertions.assertThat(observed.id).isEqualTo(source.id)
    Assertions.assertThat(observed.offenderCrn).isEqualTo(source.offender.crn)
    Assertions.assertThat(observed.contactType).isEqualTo(source.contactType?.code)
    Assertions.assertThat(observed.contactOutcome).isEqualTo(source.contactOutcomeType?.code)
    Assertions.assertThat(observed.provider).isEqualTo(source.provider?.code)
    Assertions.assertThat(observed.team).isEqualTo(source.team?.code)
    Assertions.assertThat(observed.staff).isEqualTo(source.staff?.code)
    Assertions.assertThat(observed.officeLocation).isEqualTo(source.officeLocation?.code)
    Assertions.assertThat(observed.contactDate).isEqualTo(source.contactDate)
    Assertions.assertThat(observed.contactStartTime).isEqualTo(source.contactStartTime)
    Assertions.assertThat(observed.contactEndTime).isEqualTo(source.contactEndTime)
    Assertions.assertThat(observed.alert).isEqualTo(source.alert)
    Assertions.assertThat(observed.sensitive).isEqualTo(source.sensitive)
    Assertions.assertThat(observed.notes).isEqualTo(source.notes)
    Assertions.assertThat(observed.description).isEqualTo(source.description)
  }
}
