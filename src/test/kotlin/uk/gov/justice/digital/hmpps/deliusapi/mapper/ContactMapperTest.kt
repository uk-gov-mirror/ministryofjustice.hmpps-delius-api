package uk.gov.justice.digital.hmpps.deliusapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

class ContactMapperTest {

  @Test
  fun `Mapping from entity to dto`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toDto(source)

    assertThat(observed)
      .hasProperty(ContactDto::id, source.id)
      .hasProperty(ContactDto::offenderCrn, source.offender.crn)
      .hasProperty(ContactDto::type, source.type.code)
      .hasProperty(ContactDto::outcome, source.outcome?.code)
      .hasProperty(ContactDto::enforcement, source.enforcements[0].action?.code!!)
      .hasProperty(ContactDto::provider, source.provider?.code)
      .hasProperty(ContactDto::team, source.team?.code)
      .hasProperty(ContactDto::staff, source.staff?.code)
      .hasProperty(ContactDto::officeLocation, source.officeLocation?.code)
      .hasProperty(ContactDto::date, source.date)
      .hasProperty(ContactDto::startTime, source.startTime)
      .hasProperty(ContactDto::endTime, source.endTime)
      .hasProperty(ContactDto::alert, source.alert)
      .hasProperty(ContactDto::sensitive, source.sensitive)
      .hasProperty(ContactDto::notes, source.notes)
      .hasProperty(ContactDto::description, source.description)
      .hasProperty(ContactDto::eventId, source.event?.id!!)
      .hasProperty(ContactDto::requirementId, source.requirement?.id!!)
  }

  @Test
  fun `Mapping from entity to update`() {
    val source = Fake.contact()
    val observed = ContactMapper.INSTANCE.toUpdate(source)

    assertThat(observed)
      .hasProperty(UpdateContact::outcome, source.outcome?.code)
      .hasProperty(UpdateContact::enforcement, source.enforcements[0].action?.code!!)
      .hasProperty(UpdateContact::provider, source.provider?.code)
      .hasProperty(UpdateContact::team, source.team?.code)
      .hasProperty(UpdateContact::staff, source.staff?.code)
      .hasProperty(UpdateContact::officeLocation, source.officeLocation?.code)
      .hasProperty(UpdateContact::date, source.date)
      .hasProperty(UpdateContact::startTime, source.startTime)
      .hasProperty(UpdateContact::endTime, source.endTime)
      .hasProperty(UpdateContact::alert, source.alert)
      .hasProperty(UpdateContact::sensitive, source.sensitive)
      .hasProperty(UpdateContact::notes, null)
      .hasProperty(UpdateContact::description, source.description)
  }
}
