package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@TestPropertySource(properties = ["contacts.allowed-types=${Fake.ALLOWED_CONTACT_TYPES}"])
class NewContactValidationTest : ValidationTest<NewContact>() {

  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid new contact`(case: ValidationTestCase<NewContact>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid new contact`(case: ValidationTestCase<NewContact>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<NewContact>()
      .setValid()
      .kitchenSink()
      .string(NewContact::outcome) { it.isNull().length(1).length(10) }
      .string(NewContact::officeLocation) { it.isNull() }
      .time(NewContact::endTime) { it.isNull() }
      .allNull(NewContact::startTime, NewContact::endTime)
      .string(NewContact::notes) { it.isNull().empty() }
      .string(NewContact::description) { it.isNull().empty() }
      .number(NewContact::requirementId) { it.isNull() }
      .allNull(NewContact::requirementId, NewContact::eventId)
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<NewContact>()
      .string(NewContact::offenderCrn) { it.empty().blank().value("bacon", "not a valid crn") }
      .string(NewContact::type) { it.empty().blank().value("bacon", "not an allowed contact type") }
      .string(NewContact::outcome) { it.empty().blank().length(11) }
      .string(NewContact::provider) { it.empty().blank().length(2).length(4) }
      .string(NewContact::team) { it.empty().blank().length(5).length(7) }
      .string(NewContact::staff) { it.empty().blank().length(6).length(8) }
      .string(NewContact::officeLocation) { it.empty().blank().length(6).length(8) }
      .time(NewContact::endTime) { it.dependent(NewContact::startTime).before(NewContact::startTime) }
      .string(NewContact::notes) { it.length(4001) }
      .string(NewContact::description) { it.length(201) }
      .number(NewContact::eventId) { it.zero().negative() }
      .number(NewContact::requirementId) { it.zero().negative().dependent(NewContact::eventId) }
      .number(NewContact::nsiId) { it.zero().negative().exclusive(34563, 12345, NewContact::requirementId) }
      .cases
  }
}
