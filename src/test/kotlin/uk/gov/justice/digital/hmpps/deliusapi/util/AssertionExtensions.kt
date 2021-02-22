package uk.gov.justice.digital.hmpps.deliusapi.util

import org.assertj.core.api.ObjectAssert
import org.assertj.core.api.RecursiveComparisonAssert
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.reflect.KProperty1

fun RecursiveComparisonAssert<*>.comparingDateTimesToNearest(unit: ChronoUnit, n: Int = 10): RecursiveComparisonAssert<*> =
  this
    .withEqualsForType({ a, b -> unit.between(a, b) <= n }, LocalDateTime::class.java)
    .withEqualsForType({ a, b -> unit.between(a, b) <= n }, LocalTime::class.java)

fun RecursiveComparisonAssert<*>.comparingDateTimesToNearestSecond() = comparingDateTimesToNearest(ChronoUnit.SECONDS)

fun <T : Any, P> ObjectAssert<T>.hasProperty(property: KProperty1<T, P>, expected: P): ObjectAssert<T> =
  this.describedAs(property.name).returns(expected) { property.get(it) }
