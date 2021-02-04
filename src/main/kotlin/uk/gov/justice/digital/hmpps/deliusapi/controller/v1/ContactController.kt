package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.jetbrains.annotations.NotNull
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.Contact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.service.IContactService
import javax.validation.Valid

@RestController
@RequestMapping(value = ["v1/contact"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactController(private val service: IContactService) {

  @PostMapping
  @ApiOperation(
    value = "Creates a new contact",
    response = Contact::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "The contact has been successfully created.",
        response = Contact::class
      )
    ]
  )
  fun create(@NotNull @Valid @RequestBody body: NewContact): Contact = service.createContact(body)
}
