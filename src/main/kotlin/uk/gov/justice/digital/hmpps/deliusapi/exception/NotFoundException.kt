package uk.gov.justice.digital.hmpps.deliusapi.exception

class NotFoundException(val entity: String, val clause: String, val id: Any) :
  RuntimeException("A $entity with $clause '$id' cannot be found") {
  companion object {
    inline fun <reified T> byId(id: Any) =
      NotFoundException(T::class.simpleName!!, "id", id)
  }
}
