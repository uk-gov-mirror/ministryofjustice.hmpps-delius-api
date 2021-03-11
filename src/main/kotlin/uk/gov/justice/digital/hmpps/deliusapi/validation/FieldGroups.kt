package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [FieldGroupsValidator::class])
@Retention(AnnotationRetention.RUNTIME)
annotation class FieldGroups(
  val message: String = "",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DependentFields(vararg val names: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExclusiveFields(vararg val names: String)

private enum class FieldGroupType { DEPENDENT, EXCLUSIVE }

private data class FieldGroupValidationCase(
  val type: FieldGroupType,
  val member: KProperty1<Any, *>,
  val dependents: List<KProperty1<Any, *>>,
)

fun KClass<Any>.getProperty(name: String) = memberProperties
  .find { it.name == name } ?: throw RuntimeException("$name does not exist on $simpleName")

class FieldGroupsValidator : ConstraintValidator<FieldGroups, Any> {
  override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    val kClass = value.javaClass.kotlin
    val cases = kClass.getAnnotatedMembers(DependentFields::class, ExclusiveFields::class)
      .map { (annotation, member) ->
        when (annotation) {
          is DependentFields -> FieldGroupValidationCase(
            FieldGroupType.DEPENDENT,
            member,
            annotation.names.map { kClass.getProperty(it) }
          )
          is ExclusiveFields -> FieldGroupValidationCase(
            FieldGroupType.EXCLUSIVE,
            member,
            annotation.names.map { kClass.getProperty(it) }
          )
          else -> throw RuntimeException("Unknown member group annotation ${annotation.annotationClass.simpleName}")
        }
      }

    context.disableDefaultConstraintViolation()
    var result = true
    for (case in cases) {
      result = result && case.assert(value, context)
    }

    return result
  }

  private fun FieldGroupValidationCase.assert(subject: Any, context: ConstraintValidatorContext): Boolean {
    val value = member.get(subject)
    val dependentNames = dependents.joinToString(", ") { it.name }
    val dependents = dependents.map { it.get(subject) }

    val (message, result) = when (type) {
      FieldGroupType.DEPENDENT ->
        "cannot be provided without also providing $dependentNames" to
          (value == null || dependents.all { it != null })
      FieldGroupType.EXCLUSIVE ->
        "cannot be provided when $dependentNames is also provided" to
          (value == null || dependents.all { it == null })
    }

    if (result) {
      return true
    }

    context
      .buildConstraintViolationWithTemplate(message)
      .addPropertyNode(member.name)
      .addConstraintViolation()
    return false
  }
}
