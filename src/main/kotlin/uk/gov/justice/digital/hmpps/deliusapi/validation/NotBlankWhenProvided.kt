package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NotBlankWhenProvidedValidator::class])
annotation class NotBlankWhenProvided(
  val message: String = "must not be empty or blank",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

class NotBlankWhenProvidedValidator : ConstraintValidator<NotBlankWhenProvided, String> {
  override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    return value.isNotBlank()
  }
}
