package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class SwaggerConfig {
  @Bean
  fun deliusApi(buildProperties: BuildProperties): Docket =
    Docket(DocumentationType.SWAGGER_2)
      .useDefaultResponseMessages(false)
      .apiInfo(apiInfo(buildProperties))
      .select()
      .apis(RequestHandlerSelectors.any())
      .paths(
        PathSelectors.regex("(\\/info.*)")
          .or(
            PathSelectors.regex("(\\/health)")
              .or(PathSelectors.regex("(\\/v1.*)"))
          )
      )
      .build()

  private fun contactInfo(): Contact =
    Contact(
      "HMPPS Digital Studio",
      "",
      "dps-hmpps@digital.justice.gov.uk"
    )

  private fun apiInfo(buildProperties: BuildProperties): ApiInfo =
    ApiInfo(
      "Delius API",
      "Delius API Documentation",
      buildProperties.version,
      "",
      contactInfo(),
      "Open Government Licence v3.0",
      "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/",
      emptyList()
    )
}
