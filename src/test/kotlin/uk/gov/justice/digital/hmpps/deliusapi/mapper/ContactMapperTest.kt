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
    Assertions.assertThat(observed.type).isEqualTo(source.type?.code)
    Assertions.assertThat(observed.outcome).isEqualTo(source.outcome?.code)
    Assertions.assertThat(observed.provider).isEqualTo(source.provider?.code)
    Assertions.assertThat(observed.team).isEqualTo(source.team?.code)
    Assertions.assertThat(observed.staff).isEqualTo(source.staff?.code)
    Assertions.assertThat(observed.officeLocation).isEqualTo(source.officeLocation?.code)
    Assertions.assertThat(observed.date).isEqualTo(source.date)
    Assertions.assertThat(observed.startTime).isEqualTo(source.startTime)
    Assertions.assertThat(observed.endTime).isEqualTo(source.endTime)
    Assertions.assertThat(observed.alert).isEqualTo(source.alert)
    Assertions.assertThat(observed.sensitive).isEqualTo(source.sensitive)
    Assertions.assertThat(observed.notes).isEqualTo(source.notes)
    Assertions.assertThat(observed.description).isEqualTo(source.description)
    Assertions.assertThat(observed.eventId).isNotNull.isEqualTo(source.event?.id)
    Assertions.assertThat(observed.requirementId).isNotNull.isEqualTo(source.requirement?.id)
  }
}
