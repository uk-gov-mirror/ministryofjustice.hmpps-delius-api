package uk.gov.justice.digital.hmpps.deliusapi.controller.secure

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

class Dummy(val message: String)

@RestController
@RequestMapping(value = ["secure"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DummyController {

  @GetMapping(value = ["/dummy"])
  fun dummy(): Dummy {
    return Dummy("hello world")
  }
}
