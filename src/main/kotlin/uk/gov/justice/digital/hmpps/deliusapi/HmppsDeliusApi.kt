package uk.gov.justice.digital.hmpps.deliusapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("uk.gov.justice.digital.hmpps.deliusapi.config")
class HmppsDeliusApi

fun main(args: Array<String>) {
  runApplication<HmppsDeliusApi>(*args)
}
