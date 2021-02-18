package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass
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

class FieldGroupsValidator : ConstraintValidator<FieldGroups, Any> {
  override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    val kClass = value.javaClass.kotlin
    val properties = kClass.memberProperties

    fun get(name: String) =
      (properties.find { it.name == name } ?: throw RuntimeException("$name does not exist on ${kClass.simpleName}")).get(value)

    context.disableDefaultConstraintViolation()

    fun constraint(result: Boolean, memberName: String, message: () -> String): Boolean {
      if (result) {
        return true
      }
      context
        .buildConstraintViolationWithTemplate(message())
        .addPropertyNode(memberName)
        .addConstraintViolation()
      return false
    }

    return kClass.getAnnotatedMembers(DependentFields::class)
      .flatMap { (annotation, member) ->
        when (annotation) {
          is DependentFields -> annotation.names.map { name -> name to member }
          else -> throw RuntimeException("Unknown member group annotation ${annotation.annotationClass.simpleName}")
        }
      }
      .all { (name, member) ->
        constraint(member.get(value) == null || get(name) != null, member.name) {
          "Cannot specify ${member.name} without $name"
        }
      }
  }
}
