package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.deliusapi.controller.extensions.applyPatch
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.nsi.NsiService
import uk.gov.justice.digital.hmpps.deliusapi.validation.validOrThrow
import javax.validation.Valid
import javax.validation.Validator
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Api(tags = ["NSI v1"], description = "NSI API")
@RestController
@RequestMapping(value = ["v1/nsi"], produces = [MediaType.APPLICATION_JSON_VALUE])
class NsiController(
  private val service: NsiService,
  private val objectMapper: ObjectMapper,
  private val validator: Validator
) {

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
  fun createNsi(@NotNull @Valid @RequestBody body: NewNsi): ResponseEntity<NsiDto> {
    return status(HttpStatus.CREATED).body(service.createNsi(body))
  }

  @PatchMapping(path = ["{id}"], consumes = ["application/json-patch+json", MediaType.APPLICATION_JSON_VALUE])
  @ApiOperation(
    value = "Patches an existing NSI by id",
    response = NsiDto::class,
  )
  @ApiResponses(
    value = [
      ApiResponse(
        code = 200,
        message = "The NSI was successfully patched.",
        response = NsiDto::class
      )
    ]
  )
  fun patchNsi(
    @PathVariable @Positive id: Long,
    @RequestBody patch: JsonPatch
  ): NsiDto {
    val update = service.getUpdateNsi(id)
    val patchedUpdate = objectMapper.applyPatch("nsi", patch, update)

    // HACK: we need the referral date for validation but it is immutable -> it must not be patched
    if (patchedUpdate.referralDate != update.referralDate) {
      throw BadRequestException("Cannot update the referral date")
    }

    validator.validOrThrow(patchedUpdate)
    return service.updateNsi(id, patchedUpdate)
  }
}
