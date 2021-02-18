package uk.gov.justice.digital.hmpps.deliusapi.validation

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = [AllowedValuesValidator::class])
annotation class AllowedValues(
  val value: String,
  val message: String = "must match one of the following values: {allowedValues}",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

class AllowedValuesValidator(
  var configurableBeanFactory: ConfigurableBeanFactory
) : ConstraintValidator<AllowedValues, String> {

  private lateinit var allowedValues: List<String>

  override fun initialize(annotation: AllowedValues) {
    val resolved = configurableBeanFactory.resolveEmbeddedValue(annotation.value) ?: "" // resolve values from config
    allowedValues = if (resolved.isEmpty()) emptyList() else resolved.split(",")
  }

  override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
    if (context is HibernateConstraintValidatorContext) {
      context.unwrap(HibernateConstraintValidatorContext::class.java)
        .addMessageParameter("allowedValues", allowedValues)
    }

    if (value == null) return true
    return allowedValues.isEmpty() || allowedValues.contains(value)
  }
}
