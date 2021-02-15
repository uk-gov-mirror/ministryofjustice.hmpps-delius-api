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
  fun `Invalid new contact`() {
    val subject = Fake.newContact(
      object {
        val offenderCrn = "bacon"
        val contactType = ""
        val contactOutcome = ""
        val provider = "AA"
        val team = "AAAAA"
        val staff = "AAAAAA"
        val officeLocation = "AAAAAA"
        val contactStartTime = LocalTime.NOON
        val contactEndTime = LocalTime.MIDNIGHT
        val notes = Fake.faker.lorem().characters(4001)
        val eventId = 0L
        val requirementId = 0L
      }
    )
    val result = whenValidating(subject)

    Assertions.assertThat(result)
      .describedAs("validating $subject")
      .containsOnly(
        "provider",
        "contactType",
        "contactOutcome",
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
    val subject = Fake.newContact(
      object {
        val eventId = null
        val requirementId = 1L
      }
    )

    val result = whenValidating(subject)
    Assertions.assertThat(result)
      .describedAs("validating $subject")
      .containsOnly("eventProvidedWithRequirement")
  }

  private fun whenValidating(subject: NewContact): List<String> {
    val result = validator.validate(subject)
    return result.map {
      val property = it.propertyPath.toString()
      if (property == "") it.message else property
    }.distinct()
  }
}
