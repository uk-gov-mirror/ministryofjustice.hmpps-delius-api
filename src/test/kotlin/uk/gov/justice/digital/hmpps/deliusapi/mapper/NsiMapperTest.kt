package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

class NsiMapperTest {
  @Test
  fun `Mapping from nsi to nsi dto`() {
    val source = Fake.nsi()
    source.managers.add(Fake.nsiManager(source))
    val observed = NsiMapper.INSTANCE.toDto(source)

    assertThat(observed)
      .hasProperty(NsiDto::id, source.id)
      .hasProperty(NsiDto::type, source.type?.code)
      .hasProperty(NsiDto::subType, source.subType?.code)
      .hasProperty(NsiDto::offenderCrn, source.offender?.crn)
      .hasProperty(NsiDto::eventId, source.event?.id)
      .hasProperty(NsiDto::requirementId, source.requirement?.id)
      .hasProperty(NsiDto::referralDate, source.referralDate)
      .hasProperty(NsiDto::expectedStartDate, source.expectedStartDate)
      .hasProperty(NsiDto::expectedEndDate, source.expectedEndDate)
      .hasProperty(NsiDto::startDate, source.startDate)
      .hasProperty(NsiDto::endDate, source.endDate)
      .hasProperty(NsiDto::length, source.length)
      .hasProperty(NsiDto::status, source.status?.code)
      .hasProperty(NsiDto::statusDate, source.statusDate)
      .hasProperty(NsiDto::outcome, source.outcome?.code)
      .hasProperty(NsiDto::notes, source.notes)
      .hasProperty(NsiDto::intendedProvider, source.intendedProvider?.code)
      .extracting { it.manager }
      .isNotNull
  }

  @Test
  fun `Mapping from nsi manager to nsi manager dto`() {
    val source = Fake.nsiManager()
    val observed = NsiMapper.INSTANCE.toDto(source)
    assertThat(observed)
      .hasProperty(NsiManagerDto::id, source.id)
      .hasProperty(NsiManagerDto::provider, source.provider?.code)
      .hasProperty(NsiManagerDto::team, source.team?.code)
      .hasProperty(NsiManagerDto::staff, source.staff?.code)
  }

  @Test
  fun `Mapping from nsi manager to nsi manager dto with unallocated team & staff`() {
    val source = Fake.nsiManager()
    source.team?.code = Fake.faker.bothify("?##UAT")
    source.staff?.code = "?##?##U"
    val observed = NsiMapper.INSTANCE.toDto(source)
    assertThat(observed)
      .hasProperty(NsiManagerDto::id, source.id)
      .hasProperty(NsiManagerDto::provider, source.provider?.code)
      .hasProperty(NsiManagerDto::team, null)
      .hasProperty(NsiManagerDto::staff, null)
  }
}
