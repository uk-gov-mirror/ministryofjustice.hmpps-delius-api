package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.UpdateContact

class UpdateContactValidationTest : ValidationTest<UpdateContact>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid update contact`(case: ValidationTestCase<UpdateContact>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid update contact`(case: ValidationTestCase<UpdateContact>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<UpdateContact>()
      .setValid()
      .kitchenSink()
      .string(UpdateContact::outcome) { it.length(1).length(10) }
      .string(UpdateContact::enforcement) { it.isNull().length(1).length(10) }
      .allNull(UpdateContact::outcome, UpdateContact::enforcement)
      .string(UpdateContact::officeLocation) { it.isNull() }
      .time(UpdateContact::endTime) { it.isNull() }
      .string(UpdateContact::notes) { it.isNull().empty() }
      .string(UpdateContact::description) { it.isNull().empty() }
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<UpdateContact>()
      .string(UpdateContact::outcome) { it.empty().blank().length(11) }
      .string(UpdateContact::enforcement) { it.empty().blank().length(11).dependent(UpdateContact::outcome) }
      .string(UpdateContact::provider) { it.empty().blank() }
      .string(UpdateContact::team) { it.empty().blank() }
      .string(UpdateContact::staff) { it.empty().blank() }
      .string(UpdateContact::officeLocation) { it.empty().blank() }
      .time(UpdateContact::endTime) { it.before(UpdateContact::startTime) }
      .string(UpdateContact::notes) { it.length(4001) }
      .string(UpdateContact::description) { it.length(201) }
      .cases
  }
}
