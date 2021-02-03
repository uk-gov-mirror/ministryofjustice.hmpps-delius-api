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
  private val tokenVerifyingAuthManager: TokenVerifyingAuthManager
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity) {
    http
      .oauth2ResourceServer {
        it.jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
          .authenticationManager(tokenVerifyingAuthManager)
      }
      .authorizeRequests {
        it.antMatchers("/info", "/health/**").permitAll()
          .anyRequest().authenticated()
      }
  }
}
