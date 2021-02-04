package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Dummy(val message: String)

@RestController
@RequestMapping(value = ["v1/dummy"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DummyController {

  @GetMapping
  fun get(): Dummy {
    return Dummy("hello world")
  }
}
