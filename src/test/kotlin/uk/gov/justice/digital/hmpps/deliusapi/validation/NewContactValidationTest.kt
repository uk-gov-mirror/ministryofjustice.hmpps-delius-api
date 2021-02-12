package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
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
      }
    )
    val result = validator.validate(subject)
    val invalidProperties = result.map {
      val property = it.propertyPath.toString()
      if (property == "") it.message else property
    }.distinct()

    Assertions.assertThat(invalidProperties)
      .describedAs("validating $subject")
      .containsOnly(
        "provider",
        "contactType",
        "contactOutcome",
        "team",
        "offenderCrn",
        "officeLocation",
        "staff",
        "contact start and end times must form a valid range"
      )
  }
}
