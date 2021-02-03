package uk.gov.justice.digital.hmpps.deliusapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsDeliusApi

fun main(args: Array<String>) {
  runApplication<HmppsDeliusApi>(*args)
}
