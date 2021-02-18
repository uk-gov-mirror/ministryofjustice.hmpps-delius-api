package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalTime
import javax.validation.Validation

class NewContactValidationTest {
  private val validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  fun `Valid new contact`() {
    val subject = Fake.newContact()
    val result = validator.validate(subject)
    Assertions.assertThat(result).isEmpty()
  }

  @Test
  fun `Valid new contact with no start or end time`() {
    val subject = Fake.newContact().copy(startTime = null, endTime = null)
    val result = validator.validate(subject)
    Assertions.assertThat(result).isEmpty()
  }

  @Test
  fun `Valid new contact with only start time`() {
    val subject = Fake.newContact().copy(endTime = null)
    val result = validator.validate(subject)
    Assertions.assertThat(result).isEmpty()
  }

  @Test
  fun `Invalid new contact`() {
    val subject = Fake.newContact().copy(
      offenderCrn = "bacon",
      type = "",
      outcome = Fake.faker.lorem().characters(11),
      provider = Fake.faker.lorem().characters(2),
      team = Fake.faker.lorem().characters(5),
      staff = Fake.faker.lorem().characters(6),
      officeLocation = Fake.faker.lorem().characters(6),
      startTime = LocalTime.NOON,
      endTime = LocalTime.MIDNIGHT,
      notes = Fake.faker.lorem().characters(4001),
      eventId = 0L,
      requirementId = 0L,
    )
    val result = whenValidating(subject)

    Assertions.assertThat(result)
      .describedAs("validating $subject")
      .containsOnly(
        "provider",
        "type",
        "outcome",
        "team",
        "offenderCrn",
        "officeLocation",
        "staff",
        "contact start and end times must form a valid range",
        "notes",
        "eventId",
        "requirementId"
      )
  }

  @Test
  fun `Invalid new contact when requirement id provided without event id`() {
    val subject = Fake.newContact().copy(eventId = null, requirementId = 1L)

    val result = whenValidating(subject)
    Assertions.assertThat(result)
      .describedAs("validating $subject")
      .containsOnly("eventProvidedWithRequirement")
  }

  @Test
  fun `Invalid new contact with only end time`() {
    val subject = Fake.newContact().copy(startTime = null)
    val result = whenValidating(subject)
    Assertions.assertThat(result)
      .describedAs("validating $subject")
      .containsOnly("contact start and end times must form a valid range")
  }

  @Test
  fun `Invalid new contact with start time after end time`() {
    val subject = Fake.newContact().copy(startTime = LocalTime.of(10, 20), endTime = LocalTime.of(9, 20))
    val result = whenValidating(subject)
    Assertions.assertThat(result)
      .describedAs("validating $subject")
      .containsOnly("contact start and end times must form a valid range")
  }

  private fun whenValidating(subject: NewContact): List<String> {
    val result = validator.validate(subject)
    return result.map {
      val property = it.propertyPath.toString()
      if (property == "") it.message else property
    }.distinct()
  }
}
