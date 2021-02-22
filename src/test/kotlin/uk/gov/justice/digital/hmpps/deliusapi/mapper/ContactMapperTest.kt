package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class ContactMapperTest {

  @Test
  fun `Mapping from entity to dto`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toDto(source)

    assertThat(observed.id).isEqualTo(source.id)
    assertThat(observed.offenderCrn).isEqualTo(source.offender.crn)
    assertThat(observed.type).isEqualTo(source.type?.code)
    assertThat(observed.outcome).isEqualTo(source.outcome?.code)
    assertThat(observed.provider).isEqualTo(source.provider?.code)
    assertThat(observed.team).isEqualTo(source.team?.code)
    assertThat(observed.staff).isEqualTo(source.staff?.code)
    assertThat(observed.officeLocation).isEqualTo(source.officeLocation?.code)
    assertThat(observed.date).isEqualTo(source.date)
    assertThat(observed.startTime).isEqualTo(source.startTime)
    assertThat(observed.endTime).isEqualTo(source.endTime)
    assertThat(observed.alert).isEqualTo(source.alert)
    assertThat(observed.sensitive).isEqualTo(source.sensitive)
    assertThat(observed.notes).isEqualTo(source.notes)
    assertThat(observed.description).isEqualTo(source.description)
    assertThat(observed.eventId).isNotNull.isEqualTo(source.event?.id)
    assertThat(observed.requirementId).isNotNull.isEqualTo(source.requirement?.id)
  }
}
