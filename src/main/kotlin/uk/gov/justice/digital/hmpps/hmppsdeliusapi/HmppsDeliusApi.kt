package uk.gov.justice.digital.hmpps.hmppsdeliusapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsDeliusApi

fun main(args: Array<String>) {
  runApplication<HmppsDeliusApi>(*args)
}
