package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jackson.jsonpointer.JsonPointer
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.ReplaceOperation
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsi
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.service.nsi.NsiService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import javax.validation.Validator

@ExtendWith(MockitoExtension::class)
class NsiControllerTest {
  @Mock private lateinit var service: NsiService
  @Spy private var mapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
  @Mock private lateinit var validator: Validator
  @Captor private lateinit var updateCaptor: ArgumentCaptor<UpdateNsi>
  @InjectMocks private lateinit var subject: NsiController

  @Test
  fun `Creating nsi`() {
    val request = Fake.newNsi()
    val dto = Fake.nsiDto()

    whenever(service.createNsi(request)).thenReturn(dto)

    val observed = subject.create(request).body
    Assertions.assertThat(observed).isSameAs(dto)
  }

  @Test
  fun `Patching nsi`() {
    val id = Fake.faker.number().randomNumber()
    val existing = Fake.updateNsi()
    val dto = Fake.nsiDto()

    val op = ReplaceOperation(JsonPointer.of("status"), TextNode("next-status"))
    val patch = JsonPatch(listOf(op))
    whenever(service.getUpdateNsi(id)).thenReturn(existing)
    whenever(validator.validate<UpdateNsi>(ArgumentMatchers.any())).thenReturn(emptySet())
    whenever(service.updateNsi(ArgumentMatchers.eq(id), capture(updateCaptor))).thenReturn(dto)

    val observed = subject.patch(id, patch)
    Assertions.assertThat(observed).isSameAs(dto)
    Assertions.assertThat(updateCaptor.value)
      .usingRecursiveComparison()
      .isEqualTo(existing.copy(status = "next-status"))
  }

  @Test
  fun `Attempting to patch nsi referral date`() {
    val id = Fake.faker.number().randomNumber()
    val existing = Fake.updateNsi()

    val op = ReplaceOperation(
      JsonPointer.of("referralDate"),
      TextNode(existing.referralDate.plusDays(1).toString())
    )
    val patch = JsonPatch(listOf(op))
    whenever(service.getUpdateNsi(id)).thenReturn(existing)

    assertThrows<BadRequestException>("Cannot update the referral date") { subject.patch(id, patch) }
  }
}
