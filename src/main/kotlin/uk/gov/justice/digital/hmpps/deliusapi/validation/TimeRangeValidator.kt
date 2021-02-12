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
  val message: String,
  val groups: Array<KClass<*>> = arrayOf(),
  val payload: Array<KClass<out Payload>> = arrayOf()
)

@Target(AnnotationTarget.FIELD)
annotation class StartTime

@Target(AnnotationTarget.FIELD)
annotation class EndTime

class TimeRangeValidator : ConstraintValidator<TimeRange, Any> {
  override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return false
    }

    val map = value.javaClass.kotlin.declaredMemberProperties
      .mapNotNull { member ->
        val annotations = member.javaField?.annotations
        val attr = annotations?.find { it is StartTime } ?: annotations?.find { it is EndTime }
        if (attr != null) Pair(attr.annotationClass, member.get(value) as LocalTime?) else null
      }.toMap()

    val start = map[StartTime::class] ?: throw RuntimeException("Cannot determine start time for time rage validation")
    val end = map[EndTime::class] ?: throw RuntimeException("Cannot determine end time for time rage validation")

    return start == end || end.isAfter(start)
  }
}
