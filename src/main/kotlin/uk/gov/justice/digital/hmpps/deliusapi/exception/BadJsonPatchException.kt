package uk.gov.justice.digital.hmpps.deliusapi.exception

class BadJsonPatchException(
  val type: String,
  cause: Exception,
) : RuntimeException("Invalid JSON patch $type document", cause)
