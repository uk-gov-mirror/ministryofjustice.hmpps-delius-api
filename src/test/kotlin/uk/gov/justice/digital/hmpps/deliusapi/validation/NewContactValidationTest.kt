package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
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
        val offenderId = 0
        val contactType = ""
        val contactOutcome = ""
        val provider = "AA"
        val team = "AAAAA"
        val staff = "AAAAAA"
        val officeLocation = "AAAAAA"
      }
    )
    val result = validator.validate(subject)
    val invalidProperties = result.map { it.propertyPath.toString() }.distinct()
    Assertions.assertThat(invalidProperties).containsOnly(
      "provider",
      "contactType",
      "contactOutcome",
      "team",
      "offenderId",
      "officeLocation",
      "staff"
    )
  }
}
