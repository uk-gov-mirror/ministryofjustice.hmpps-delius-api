package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration(
  private val tokenVerifyingAuthManager: TokenVerifyingAuthManager,
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    http
      .oauth2ResourceServer {
        it.jwt().authenticationManager(tokenVerifyingAuthManager)
      }
      .authorizeRequests {
        it.antMatchers(
          "/info",
          "/health/**",
          "/v2/api-docs",
          "/v3/api-docs",
          "/swagger-ui/**",
          "/swagger-resources",
          "/swagger-resources/configuration/ui",
          "/swagger-resources/configuration/security",
          "/h2-console/**"
        ).permitAll()
          .anyRequest().authenticated()
      }
      .csrf().disable()
  }
}
