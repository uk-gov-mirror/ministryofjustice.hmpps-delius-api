package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.service.NsiService
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(value = ["v1/nsi"], produces = [MediaType.APPLICATION_JSON_VALUE])
class NsiController(private val service: NsiService) {
  @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(
    value = "Creates a new NSI",
    response = NsiDto::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 201,
        message = "The NSI has been successfully created.",
        response = NsiDto::class
      )
    ]
  )
  fun create(@NotNull @Valid @RequestBody body: NewNsi): ResponseEntity<NsiDto> {
    return status(HttpStatus.CREATED).body(service.createNsi(body))
  }
}
