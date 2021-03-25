package uk.gov.justice.digital.hmpps.deliusapi.util

import org.assertj.core.api.AbstractObjectAssert
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

fun <T, P, Self : AbstractObjectAssert<Self, T>> Self.hasProperty(property: KProperty1<T, P>, expected: P): Self =
  describedAs(property.name).returns(expected) { property.get(it) }

fun <T, U> AbstractObjectAssert<*, T>.extractingObject(fn: (subject: T) -> U): ObjectAssert<U> =
  extracting(fn) as ObjectAssert<U>
