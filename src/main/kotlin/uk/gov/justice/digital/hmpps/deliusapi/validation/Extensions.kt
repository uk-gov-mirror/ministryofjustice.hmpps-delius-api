package uk.gov.justice.digital.hmpps.deliusapi.validation

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

fun KClass<Any>.getAnnotatedMembers(vararg annotationTypes: KClass<*>): List<Pair<Annotation, KProperty1<Any, *>>> {
  return this.declaredMemberProperties
    .flatMap { member ->
      member.javaField?.annotations
        ?.filter { fa -> annotationTypes.any { it.isInstance(fa) } }
        ?.map { Pair(it, member) } ?: listOf()
    }
}
