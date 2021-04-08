package uk.gov.justice.digital.hmpps.deliusapi.util

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.ObjectAssert
import kotlin.reflect.KProperty1

fun <T : Throwable> assertThatException(value: T) = ObjectAssert(value)

fun <T, P, Self : AbstractObjectAssert<Self, T>> Self.hasProperty(property: KProperty1<T, P>, expected: P): Self =
  describedAs(property.name).returns(expected) { property.get(it) }

fun <T, U> AbstractObjectAssert<*, T>.extractingObject(fn: (subject: T) -> U): ObjectAssert<U> =
  extracting(fn) as ObjectAssert<U>
