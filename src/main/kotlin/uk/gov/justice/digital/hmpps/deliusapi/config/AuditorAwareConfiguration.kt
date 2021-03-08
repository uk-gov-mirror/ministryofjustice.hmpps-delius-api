package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.service.security.SecurityUserContext
import java.util.Optional

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
class AuditorAwareConfiguration(
  private val securityUserContext: SecurityUserContext
) : AuditorAware<Long> {
  override fun getCurrentAuditor(): Optional<Long> {
    return Optional.of(securityUserContext.getCurrentDeliusUserId())
  }
}
