package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsi
import java.time.LocalDate
import java.time.LocalDateTime

class UpdateNsiValidationTest : ValidationTest<UpdateNsi>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid update nsi`(case: ValidationTestCase<UpdateNsi>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid update nsi`(case: ValidationTestCase<UpdateNsi>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<UpdateNsi>()
      .setValid()
      .kitchenSink()
      .allNull(UpdateNsi::outcome, UpdateNsi::endDate)
      .string(UpdateNsi::notes) { it.isNull() }
      .add("status date and referral date can be on same day") {
        it.copy(
          statusDate = LocalDateTime.of(2021, 3, 4, 0, 0, 1),
          referralDate = LocalDate.of(2021, 3, 4),
          startDate = null,
          endDate = null,
          expectedStartDate = null,
          expectedEndDate = null,
          outcome = null,
        )
      }
      .add("status date and referral date can be exactly same") {
        it.copy(
          statusDate = LocalDateTime.of(2021, 3, 4, 0, 0),
          referralDate = LocalDate.of(2021, 3, 4),
          startDate = null,
          endDate = null,
          expectedStartDate = null,
          expectedEndDate = null,
          outcome = null,
        )
      }
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<UpdateNsi>()
      .date(UpdateNsi::referralDate, strict = false) { it.tomorrow() }
      .date(UpdateNsi::expectedStartDate) { it.before(UpdateNsi::referralDate) }
      .date(UpdateNsi::expectedEndDate) { it.before(UpdateNsi::expectedStartDate).dependent(UpdateNsi::expectedStartDate) }
      .date(UpdateNsi::startDate) { it.before(UpdateNsi::referralDate) }
      .date(UpdateNsi::startDate, strict = false) { it.tomorrow() }
      .date(UpdateNsi::endDate) { it.before(UpdateNsi::startDate).dependent(UpdateNsi::startDate) }
      .number(UpdateNsi::length) { it.zero().negative() }
      .string(UpdateNsi::status) { it.empty().blank().length(21) }
      .dateTime(UpdateNsi::statusDate) { it.beforeDate(UpdateNsi::referralDate).tomorrow() }
      .string(UpdateNsi::outcome) { it.empty().blank().length(101).dependent(UpdateNsi::endDate) }
      .string(UpdateNsi::notes) { it.length(4001) }
      .cases
  }
}
