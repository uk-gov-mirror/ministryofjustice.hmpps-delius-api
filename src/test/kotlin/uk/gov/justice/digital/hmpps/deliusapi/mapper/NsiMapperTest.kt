package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

class NsiMapperTest {
  @Test
  fun `Mapping from entity to dto`() {
    val source = Fake.nsi()
    val observed = NsiMapper.INSTANCE.toDto(source)

    assertThat(observed.id).isEqualTo(source.id)
    assertThat(observed.type).isEqualTo(source.type?.code)
    assertThat(observed.subType).isEqualTo(source.subType?.code)
    assertThat(observed.offenderCrn).isEqualTo(source.offender?.crn)
    assertThat(observed.eventId).isEqualTo(source.event?.id)
    assertThat(observed.requirementId).isEqualTo(source.requirement?.id)
    assertThat(observed.referralDate).isEqualTo(source.referralDate)
    assertThat(observed.expectedStartDate).isEqualTo(source.expectedStartDate)
    assertThat(observed.expectedEndDate).isEqualTo(source.expectedEndDate)
    assertThat(observed.startDate).isEqualTo(source.startDate)
    assertThat(observed.endDate).isEqualTo(source.endDate)
    assertThat(observed.length).isEqualTo(source.length)
    assertThat(observed.status).isEqualTo(source.status?.code)
    assertThat(observed.statusDate).isEqualTo(source.statusDate)
    assertThat(observed.outcome).isEqualTo(source.outcome?.code)
    assertThat(observed.notes).isEqualTo(source.notes)
    assertThat(observed.intendedProvider).isEqualTo(source.intendedProvider?.code)

    val managers = source.managers?.map {
      NsiManagerDto(it.id, it.staff?.code ?: "", it.team?.code ?: "", it.provider?.code ?: "")
    }

    assertThat(observed.managers).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(managers)
  }
}
