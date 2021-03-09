package uk.gov.justice.digital.hmpps.deliusapi.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Size
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@ReportAsSingleViolation
@NotBlankWhenProvided
@Size(max = 3)
annotation class ProviderCode(
  val message: String = "must be a valid provider code",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
