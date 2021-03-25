package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsiManager

class UpdateNsiManagerValidationTest : ValidationTest<UpdateNsiManager>() {
  @ParameterizedTest
  @MethodSource("validCases")
  fun `Valid update nsi manager`(case: ValidationTestCase<UpdateNsiManager>) = assertValid(case)

  @ParameterizedTest
  @MethodSource("invalidCases")
  fun `Invalid update nsi manager`(case: ValidationTestCase<UpdateNsiManager>) = assertInvalid(case)

  companion object {
    @JvmStatic
    fun validCases() = ValidationTestCaseBuilder.fromFake<UpdateNsiManager>()
      .setValid()
      .kitchenSink()
      .cases

    @JvmStatic
    fun invalidCases() = ValidationTestCaseBuilder.fromFake<UpdateNsiManager>()
      .string(UpdateNsiManager::staff) { it.empty().blank().length(8) }
      .string(UpdateNsiManager::team) { it.empty().blank().length(7) }
      .string(UpdateNsiManager::transferReason) { it.empty().blank() }
      .cases
  }
}
