package uk.gov.justice.digital.hmpps.deliusapi.config

import io.swagger.annotations.ApiOperation
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.Contact
import springfox.documentation.service.HttpAuthenticationScheme
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.OperationBuilderPlugin
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.common.SwaggerPluginSupport
import java.time.LocalTime

@Configuration
class SwaggerConfig {
  @Bean
  fun deliusApi(buildProperties: BuildProperties): Docket =
    Docket(DocumentationType.OAS_30)
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
      .securitySchemes(listOf(bearerToken()))
      .securityContexts(listOf(securityContext()))
      .directModelSubstitute(LocalTime::class.java, String::class.java)
      .forCodeGeneration(true)

  private fun bearerToken() = HttpAuthenticationScheme
    .JWT_BEARER_BUILDER
    .name("Bearer")
    .build()

  private fun securityContext() = SecurityContext.builder()
    .securityReferences(listOf(defaultAuth()))
    .operationSelector {
      it.requestMappingPattern().matches("\\/v1.*".toRegex())
    }
    .build()

  private fun defaultAuth() = SecurityReference(
    "Bearer", arrayOf(AuthorizationScope("global", "global"))
  )

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

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000) // we need this to run after annotation & default readers
class NiceOperationReader : OperationBuilderPlugin {
  override fun apply(context: OperationContext) {
    // If nickname exists, populate the value of nickname annotation into uniqueId
    // Otherwise clean up health & info cases
    val uniqueId = context.findControllerAnnotation(ApiOperation::class.java)
      .map { it.nickname }
      .orElse(null)
      ?: getNiceApiName(context)

    context.operationBuilder()
      .uniqueId(uniqueId)
      .codegenMethodNameStem(uniqueId)
  }

  override fun supports(delimiter: DocumentationType) =
    SwaggerPluginSupport.pluginDoesApply(delimiter)

  private fun getNiceApiName(context: OperationContext): String {
    return if (context.name.matches(UNNAMED_ENDPOINT)) {
      // special case for health & info endpoints
      context.httpMethod().toCrud() +
        context.requestMappingPattern()
          .split("/")
          .joinToString("") { it.capitalize() }
    } else context.name
  }

  companion object {
    private val UNNAMED_ENDPOINT = "^handle(?:_\\d+)?$".toRegex()

    private fun HttpMethod.toCrud() = when (this) {
      HttpMethod.GET -> "get"
      HttpMethod.POST -> "create"
      HttpMethod.PUT -> "update"
      HttpMethod.PATCH -> "patch"
      HttpMethod.DELETE -> "delete"
      else -> throw RuntimeException("Unsupported CRUD HTTP method $this")
    }
  }
}
