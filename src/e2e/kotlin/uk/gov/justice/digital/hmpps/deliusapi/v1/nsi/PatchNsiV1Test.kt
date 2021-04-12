package uk.gov.justice.digital.hmpps.deliusapi.v1.nsi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.NsiTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newNsi
import uk.gov.justice.digital.hmpps.deliusapi.util.Operation
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

@Disabled("TODO patch tests will not work until the API accepts application/json")
class PatchNsiV1Test : EndToEndTest() {
  private lateinit var nsi: NsiDto
  private lateinit var response: NsiDto

  @Test
  fun `Patching nsi status`() {
    nsi = havingExistingNsi(NsiTestsConfiguration::active)
    val refer = configuration.newNsi(NsiTestsConfiguration::refer).copy(
      statusDate = nsi.statusDate.plusHours(1)
    )
    whenPatchingNsi(
      Operation("replace", "/status", refer.status),
      Operation("replace", "/statusDate", refer.statusDate),
    )
    assertThat(response)
      .hasProperty(NsiDto::status, refer.status)
      .hasProperty(NsiDto::statusDate, refer.statusDate)
  }

  private fun whenPatchingNsi(vararg operations: Operation) {
    response = nsiV1.safely { it.patchNsi(nsi.id, operations) }
  }
}
