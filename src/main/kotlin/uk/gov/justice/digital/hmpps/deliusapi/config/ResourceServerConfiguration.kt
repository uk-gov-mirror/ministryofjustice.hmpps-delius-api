package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration(
  private val tokenVerifyingAuthManager: TokenVerifyingAuthManager,
  private val environment: Environment,
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    val permitted = mutableListOf("/info", "/health/**")
    if (environment.activeProfiles.contains("dev")) {
      permitted.add("/h2-console/**")
      permitted.add("/")

      http.csrf().disable()
      http.headers().frameOptions().disable()
    }

    http
      .oauth2ResourceServer {
        it.jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
          .authenticationManager(tokenVerifyingAuthManager)
      }
      .authorizeRequests {
        it.antMatchers(*permitted.toTypedArray()).permitAll()
          .anyRequest().authenticated()
      }
  }
}
