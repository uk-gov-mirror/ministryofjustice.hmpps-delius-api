package uk.gov.justice.digital.hmpps.deliusapi.v1.nsi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.NsiTestsConfiguration
import uk.gov.justice.digital.hmpps.deliusapi.config.newNsi
import uk.gov.justice.digital.hmpps.deliusapi.util.Operation
import uk.gov.justice.digital.hmpps.deliusapi.util.hasProperty

class PatchNsiV1Test : NsiEndToEndTest() {
  private lateinit var existing: NsiDto
  private lateinit var response: NsiDto

  @Test
  @Transactional
  fun `Patching nsi status`() {
    existing = havingExistingNsi(NsiTestsConfiguration::active)
    val refer = configuration.newNsi(NsiTestsConfiguration::refer).copy(
      statusDate = existing.statusDate.plusSeconds(1)
    )

    whenPatchingNsi(
      Operation("replace", "/status", refer.status),
      Operation("replace", "/statusDate", refer.statusDate),
    )

    assertThat(response)
      .hasProperty(NsiDto::status, refer.status)
      .hasProperty(NsiDto::statusDate, refer.statusDate)

    shouldUpdateNsi(existing.id) {
      assertThat(it.status!!.code).isEqualTo(refer.status)
      assertThat(it.statusDate).isEqualTo(refer.statusDate)
    }

    shouldSaveLatestStatusHistory(refer.status!!)
  }

  private fun whenPatchingNsi(vararg operations: Operation) {
    response = nsiV1.safely { it.patchNsi(existing.id, operations) }
  }
}
