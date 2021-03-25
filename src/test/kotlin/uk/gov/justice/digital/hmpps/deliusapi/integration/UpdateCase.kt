package uk.gov.justice.digital.hmpps.deliusapi.integration

import kotlin.reflect.KProperty1

data class Operation(val op: String, val path: String, val value: Any? = null)

class UpdateCase<Dto>(
  vararg val operations: Operation,
  val expected: (contact: Dto) -> Map<KProperty1<Dto, *>, Any?>,
)
