package uk.gov.justice.digital.hmpps.deliusapi.config

import org.hibernate.validator.constraints.URL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  /** OAUTH2 API Rest URL endpoint ("http://localhost:8100") */
  @Value("\${tokenverification.api.base.url}") private val tokenVerificationApiBaseUrl: @URL String,
) {

  @Bean
  fun tokenVerificationApiWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl(tokenVerificationApiBaseUrl)
    .clientConnector(
      ReactorClientHttpConnector(HttpClient.create().warmupWithHealthPing(tokenVerificationApiBaseUrl))
    )
    .build()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private fun HttpClient.warmupWithHealthPing(baseUrl: String): HttpClient {
    log.info("Warming up web client for {}", baseUrl)
    warmup().block()
    log.info("Warming up web client for {} halfway through, now calling health ping", baseUrl)
    try {
      baseUrl("$baseUrl/health/ping").get().response().block(Duration.ofSeconds(30))
    } catch (e: RuntimeException) {
      log.error("Caught exception during warm up, carrying on regardless", e)
    }
    log.info("Warming up web client completed for {}", baseUrl)
    return this
  }
}
