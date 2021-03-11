package uk.gov.justice.digital.hmpps.deliusapi.security

import org.springframework.security.access.prepost.PostAuthorize
import uk.gov.justice.digital.hmpps.deliusapi.config.Authorities

@Retention(AnnotationRetention.RUNTIME)
@PostAuthorize("hasAuthority('${Authorities.PROVIDER}'.concat(returnObject.provider))")
annotation class ProviderResponseAuthority
