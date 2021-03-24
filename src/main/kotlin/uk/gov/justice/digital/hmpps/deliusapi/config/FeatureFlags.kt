package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "features")
open class FeatureFlags(
  open val tokenVerification: Boolean = false,
  open val nsiStatusHistory: Boolean = false,
)
