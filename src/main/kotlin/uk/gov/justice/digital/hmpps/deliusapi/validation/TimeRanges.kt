package uk.gov.justice.digital.hmpps.deliusapi.validation

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [TimeRangesValidator::class])
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeRanges(
  val message: String = "must be a valid time range",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)

@Target(AnnotationTarget.FIELD)
annotation class StartTime(val name: String)

@Target(AnnotationTarget.FIELD)
annotation class StartTimes(vararg val values: StartTime)

@Target(AnnotationTarget.FIELD)
annotation class EndTime(val name: String)

@Target(AnnotationTarget.FIELD)
annotation class EndTimes(vararg val values: EndTime)

private interface ComparableTemporal : Comparable<ComparableTemporal> {
  val value: Any
  val name: String

  fun isAfter(other: ComparableTemporal) = compareTo(other) > 0
}

private data class ComparableDateTime(override val value: LocalDateTime) : ComparableTemporal {
  override val name = "date time"
  override fun compareTo(other: ComparableTemporal) = when (other) {
    is ComparableDateTime -> value.compareTo(other.value)
    is ComparableDate -> value.toLocalDate().compareTo(other.value)
    else -> throw RuntimeException("Cannot compare ${other.value.javaClass.name} to LocalDateTime")
  }
}

private data class ComparableDate(override val value: LocalDate) : ComparableTemporal {
  override val name = "date"
  override fun compareTo(other: ComparableTemporal) = when (other) {
    is ComparableDate -> value.compareTo(other.value)
    is ComparableDateTime -> value.compareTo(other.value.toLocalDate())
    else -> throw RuntimeException("Cannot compare ${other.value.javaClass.name} to LocalDate")
  }
}

private data class ComparableTime(override val value: LocalTime) : ComparableTemporal {
  override val name = "time"
  override fun compareTo(other: ComparableTemporal) = when (other) {
    is ComparableTime -> value.compareTo(other.value)
    else -> throw RuntimeException("Cannot compare ${other.value.javaClass.name} to LocalTime")
  }
}

private data class TimeRangeGroup(
  val name: String,
  val start: ComparableTemporal,
  val end: ComparableTemporal,
  val startMember: String,
  val endMember: String,
)

class TimeRangesValidator : ConstraintValidator<TimeRanges, Any> {
  override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    val groups = value.javaClass.kotlin.getAnnotatedMembers(StartTime::class, EndTime::class, StartTimes::class, EndTimes::class)
      .flatMap { (k, v) ->
        when (k) {
          is StartTimes -> k.values.map { it to v }
          is EndTimes -> k.values.map { it to v }
          else -> listOf(k to v)
        }
      }
      .groupBy { (k) ->
        when (k) {
          is StartTime -> k.name
          is EndTime -> k.name
          else -> ""
        }
      }
      .filterKeys { it != "" }
      .mapNotNull { (key, annotations) ->
        val map = annotations.associateBy(
          { (k) -> k.annotationClass },
          { (_, member) ->
            when (val result = member.get(value)) {
              null -> null to member.name
              is LocalTime -> ComparableTime(result) to member.name
              is LocalDateTime -> ComparableDateTime(result) to member.name
              is LocalDate -> ComparableDate(result) to member.name
              else -> throw RuntimeException("${member.name} must be a local time, date or date time for $key time range validation")
            }
          }
        )
        val (start, startMember) = map[StartTime::class] ?: throw RuntimeException("No StartTime annotation for time range group $key")
        val (end, endMember) = map[EndTime::class] ?: throw RuntimeException("No EndTime annotation for time range group $key")

        if (start == null || end == null) null else TimeRangeGroup(key, start, end, startMember, endMember)
      }

    context.disableDefaultConstraintViolation()
    var result = true

    for (group in groups) {
      result = isValid(context, group) && result
    }

    return result
  }

  private fun isValid(context: ConstraintValidatorContext, group: TimeRangeGroup): Boolean {
    val isValid = group.start == group.end || group.end.isAfter(group.start)

    if (!isValid) {
      val message = "${group.endMember} must be after or equal to ${group.startMember}"
      for (member in listOf(group.startMember, group.endMember)) {
        context
          .buildConstraintViolationWithTemplate(message)
          .addPropertyNode(member)
          .addConstraintViolation()
      }
    }

    return isValid
  }
}
