package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ExclusiveFieldValidator::class])
annotation class Exclusive(
  val message: String = "only one can be completed",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

@Target(AnnotationTarget.FIELD)
annotation class ExclusiveField

class ExclusiveFieldValidator : ConstraintValidator<Exclusive, Any> {
  override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    val groups = value.javaClass.kotlin.getAnnotatedMembers(ExclusiveField::class)
      .mapNotNull { (_, annotations) ->
        annotations.name to annotations.get(value)
      }.toMap()

    val withValues = groups.filter { p -> p.value != null }

    val isValid = withValues.size <= 1

    context.disableDefaultConstraintViolation()

    if (!isValid) {
      context
        .buildConstraintViolationWithTemplate("Only one of ${groups.keys.joinToString(", ")} can have a value")
        .addPropertyNode(groups.keys.first())
        .addConstraintViolation()
    }

    return isValid
  }
}
