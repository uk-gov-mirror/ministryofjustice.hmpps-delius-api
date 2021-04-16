package uk.gov.justice.digital.hmpps.deliusapi.v1.nsi

import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.deliusapi.EndToEndTest
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiRepository

abstract class NsiEndToEndTest : EndToEndTest() {
  @Autowired protected lateinit var repository: NsiRepository

  protected fun shouldUpdateNsi(id: Long, assert: (nsi: Nsi) -> Unit) = withDatabase {
    val nsi = repository.findByIdOrNull(id)
      ?: throw RuntimeException("NSI with id = '$id' does not exist in the database")

    assert(nsi)

    if (!features.nsiStatusHistory) {
      logger.warn("Skipping nsi status history assertions")
    } else {
      val latestStatus = nsi.statuses.maxByOrNull { it.id }
        ?: throw RuntimeException("NSI with id = '${nsi.id}' does not have any status history records")

      val code = nsi.status!!.code
      val description = nsi.statuses.joinToString(", ") { s -> "[${s.id}] ${s.date} ${s.status!!.code}" }
      Assertions.assertThat(latestStatus.status!!.code)
        .describedAs("NSI with id = '${nsi.id}' should have status history updated to '$code', all history: $description")
        .isEqualTo(code)
    }
  }
}
