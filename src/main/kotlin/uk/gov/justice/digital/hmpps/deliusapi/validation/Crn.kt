package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@ReportAsSingleViolation
@NotBlankWhenProvided
@Pattern(regexp = "^[a-zA-Z][0-9]{6}\$")
annotation class Crn(
  val message: String = "must be a valid crn",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
