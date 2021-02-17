package uk.gov.justice.digital.hmpps.deliusapi.validation

import java.time.LocalTime
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [TimeRangeValidator::class])
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeRange(
  val name: String,
  val message: String,
  val groups: Array<KClass<*>> = arrayOf(),
  val payload: Array<KClass<out Payload>> = arrayOf()
)

@Target(AnnotationTarget.FIELD)
annotation class StartTime(val name: String)

@Target(AnnotationTarget.FIELD)
annotation class EndTime(val name: String)

class TimeRangeValidator : ConstraintValidator<TimeRange, Any> {
  private lateinit var name: String

  override fun initialize(annotation: TimeRange) {
    name = annotation.name
  }

  override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return false
    }

    val map = value.javaClass.kotlin.declaredMemberProperties
      .mapNotNull { member ->
        val annotations = member.javaField?.annotations
        val attr = annotations?.find { it is StartTime && it.name == name }
          ?: annotations?.find { it is EndTime && it.name == name }
        if (attr != null) Pair(attr.annotationClass, member.get(value) as LocalTime?) else null
      }.toMap()

    fun <T : Annotation> get(clazz: KClass<T>, field: String): LocalTime =
      map[clazz] ?: throw RuntimeException("Cannot determine $field for $name time range validation")

    val start = get(StartTime::class, "start time")
    val end = get(EndTime::class, "end time")

    return start == end || end.isAfter(start)
  }
}
