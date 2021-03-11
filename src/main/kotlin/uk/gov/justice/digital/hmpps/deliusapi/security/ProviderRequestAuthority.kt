package uk.gov.justice.digital.hmpps.deliusapi.security

import org.springframework.security.access.prepost.PreAuthorize
import uk.gov.justice.digital.hmpps.deliusapi.config.Authorities

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAuthority('${Authorities.PROVIDER}'.concat(#request.provider))")
annotation class ProviderRequestAuthority
