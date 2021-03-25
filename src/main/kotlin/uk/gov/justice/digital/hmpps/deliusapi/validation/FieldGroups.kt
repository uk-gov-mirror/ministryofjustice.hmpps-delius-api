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
annotation class FieldGroup(val type: FieldGroupType, vararg val names: String)

enum class FieldGroupType {
  /**
   * All specified fields must be provided
   */
  DEPENDENT_ALL,

  /**
   * Any specified fields must be provided
   */
  DEPENDENT_ANY,

  /**
   * Cannot be provided when any of the specified fields are provided
   */
  EXCLUSIVE_ANY
}

fun KClass<Any>.getProperty(name: String) = memberProperties
  .find { it.name == name } ?: throw RuntimeException("$name does not exist on $simpleName")

class FieldGroupsValidator : ConstraintValidator<FieldGroups, Any> {
  override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    context.disableDefaultConstraintViolation()
    var result = true
    val kClass = value.javaClass.kotlin

    for ((annotation, member) in kClass.getAnnotatedMembers(FieldGroup::class)) {
      when (annotation) {
        is FieldGroup -> result = result && assert(
          annotation.type,
          member,
          annotation.names.map { kClass.getProperty(it) },
          value,
          context
        )
        else -> throw RuntimeException("Unknown member group annotation ${annotation.annotationClass.simpleName}")
      }
    }

    return result
  }

  private fun pluralizeFields(aggregate: String, fields: List<String>): String {
    if (fields.size == 1) {
      return fields[0]
    }
    val joined = fields.dropLast(1).joinToString(", ")
    return "$joined $aggregate ${fields.last()}"
  }

  private fun assert(
    type: FieldGroupType,
    member: KProperty1<Any, *>,
    dependents: List<KProperty1<Any, *>>,
    subject: Any,
    context: ConstraintValidatorContext
  ): Boolean {
    val value = member.get(subject)
    val dependentNames = dependents.map { it.name }
    val dependentValues = dependents.map { it.get(subject) }

    val (message, result) = when (type) {
      FieldGroupType.DEPENDENT_ALL ->
        "cannot be provided without also providing ${pluralizeFields("and", dependentNames)}" to
          (value == null || dependentValues.all { it != null })
      FieldGroupType.DEPENDENT_ANY ->
        "cannot be provided without also providing ${pluralizeFields("or", dependentNames)}" to
          (value == null || dependentValues.all { it != null })
      FieldGroupType.EXCLUSIVE_ANY ->
        "cannot be provided when ${pluralizeFields("or", dependentNames)} ${if (dependentValues.size == 1) "is" else "are"} also provided" to
          (value == null || dependentValues.all { it == null })
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
