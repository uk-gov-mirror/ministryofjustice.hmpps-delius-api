package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import java.time.LocalDate
import java.time.LocalDateTime

class NewNsiValidationTest : ValidationTest<NewNsi>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid new nsi`(case: ValidationTestCase<NewNsi>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid new nsi`(case: ValidationTestCase<NewNsi>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<NewNsi>()
      .setValid()
      .kitchenSink()
      .string(NewNsi::subType) { it.isNull() }
      .number(NewNsi::requirementId) { it.isNull() }
      .allNull(NewNsi::requirementId, NewNsi::eventId)
      .allNull(NewNsi::outcome, NewNsi::endDate)
      .string(NewNsi::notes) { it.isNull() }
      .add("status date and referral date can be on same day") {
        it.copy(
          statusDate = LocalDateTime.now(),
          referralDate = LocalDate.now(),
          startDate = null,
          endDate = null,
          expectedStartDate = null,
          expectedEndDate = null,
          outcome = null,
        )
      }
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<NewNsi>()
      .string(NewNsi::type) { it.empty().blank().length(21) }
      .string(NewNsi::subType) { it.empty().blank().length(21) }
      .string(NewNsi::offenderCrn) { it.empty().blank().value("bacon", "not a valid crn") }
      .number(NewNsi::eventId) { it.zero().negative() }
      .number(NewNsi::requirementId) { it.zero().negative().dependent(NewNsi::eventId) }
      .date(NewNsi::referralDate, strict = false) { it.tomorrow() }
      .date(NewNsi::expectedStartDate) { it.before(NewNsi::referralDate) }
      .date(NewNsi::expectedEndDate) { it.before(NewNsi::expectedStartDate).dependent(NewNsi::expectedStartDate) }
      .date(NewNsi::startDate) { it.before(NewNsi::referralDate) }
      .date(NewNsi::startDate, strict = false) { it.tomorrow() }
      .date(NewNsi::endDate) { it.before(NewNsi::startDate).dependent(NewNsi::startDate) }
      .number(NewNsi::length) { it.zero().negative() }
      .string(NewNsi::status) { it.empty().blank().length(21) }
      .dateTime(NewNsi::statusDate) { it.beforeDate(NewNsi::referralDate).tomorrow() }
      .string(NewNsi::outcome) { it.empty().blank().length(101).dependent(NewNsi::endDate) }
      .string(NewNsi::notes) { it.length(4001) }
      .string(NewNsi::intendedProvider) { it.empty().blank().length(2).length(4) }
      .cases
  }
}
