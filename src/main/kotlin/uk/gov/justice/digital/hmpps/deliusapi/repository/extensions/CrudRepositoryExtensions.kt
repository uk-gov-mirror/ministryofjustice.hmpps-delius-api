package uk.gov.justice.digital.hmpps.deliusapi.repository.extensions

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.exception.NotFoundException

inline fun <reified T, ID> CrudRepository<T, ID>.findByIdOrBadRequest(id: ID): T =
  findByIdOrNull(id) ?: throw BadRequestException("Cannot find ${T::class.simpleName} with id '$id'")

inline fun <reified T, reified ID : Any> CrudRepository<T, ID>.findByIdOrNotFound(id: ID) =
  findByIdOrNull(id) ?: throw NotFoundException.byId<T>(id)
