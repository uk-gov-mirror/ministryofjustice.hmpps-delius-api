package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalTime
import javax.validation.Validator

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = ["contacts.allowed-types=C001,C002,C003"])
class NewContactValidationTest {

  @Autowired
  private lateinit var validator: Validator

  val validContact = Fake.newContact().copy(
    type = Fake.faker.options().option("C001", "C002", "C003")
  )

  @Test
  fun `Valid new contact`() {
    val subject = validContact
    val result = validator.validate(subject)
    assertThat(result).isEmpty()
  }

  @Test
  fun `Valid new contact with no start or end time`() {
    val subject = validContact.copy(startTime = null, endTime = null)
    val result = validator.validate(subject)
    assertThat(result).isEmpty()
  }

  @Test
  fun `Valid new contact with only start time`() {
    val subject = validContact.copy(endTime = null)
    val result = validator.validate(subject)
    assertThat(result).isEmpty()
  }

  @Test
  fun `Invalid new contact`() {
    val subject = validContact.copy(
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

    assertThat(result.keys)
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
    val subject = validContact.copy(eventId = null, requirementId = 1L)

    val result = whenValidating(subject)
    assertThat(result.keys)
      .describedAs("validating $subject")
      .containsOnly("eventProvidedWithRequirement")
  }

  @Test
  fun `Invalid new contact with only end time`() {
    val subject = validContact.copy(startTime = null)
    val result = whenValidating(subject)
    assertThat(result.values)
      .describedAs("validating $subject")
      .containsOnly("contact start and end times must form a valid range")
  }

  @Test
  fun `Invalid new contact with start time after end time`() {
    val subject = validContact.copy(startTime = LocalTime.of(10, 20), endTime = LocalTime.of(9, 20))
    val result = whenValidating(subject)
    assertThat(result.values)
      .describedAs("validating $subject")
      .containsOnly("contact start and end times must form a valid range")
  }

  @Test
  fun `Contact with type that is not allowed`() {
    val subject = validContact.copy(type = "OTHER")
    val result = whenValidating(subject)
    assertThat(result)
      .describedAs("validating $subject")
      .containsOnly(entry("type", "must match one of the following values: [C001, C002, C003]"))
  }

  private fun whenValidating(subject: NewContact): Map<String, String> = validator.validate(subject)
    .map {
      val property = it.propertyPath.toString()
      Pair(if (property == "") it.message else property, it.message)
    }.distinct().toMap()
}
