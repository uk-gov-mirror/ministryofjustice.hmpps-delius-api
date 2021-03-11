package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun <T> Validator.validOrThrow(subject: T) {
  val errors = validate(subject)
  if (errors.isNotEmpty()) {
    throw ConstraintViolationException(errors)
  }
}
