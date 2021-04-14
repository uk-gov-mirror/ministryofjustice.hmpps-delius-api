package uk.gov.justice.digital.hmpps.deliusapi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.deliusapi.client.api.OperationHandlerApi
import uk.gov.justice.digital.hmpps.deliusapi.client.safely
import uk.gov.justice.digital.hmpps.deliusapi.config.EndToEndTestActiveProfilesResolver
import uk.gov.justice.digital.hmpps.deliusapi.config.EndToEndTestConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(resolver = EndToEndTestActiveProfilesResolver::class)
class InstrumentationTest @Autowired constructor(
  configuration: EndToEndTestConfiguration
) {

  private val client = OperationHandlerApi(configuration.url)

  @Test
  fun `Getting health`() {
    val response = client.safely { it.getHealth(null) as Map<String, String> }
    assertThat(response)
      .isNotNull
      .containsEntry("status", "UP")
  }

  @Test
  fun `Getting info`() {
    val response = client.safely { it.getInfo(null) as Map<String, Map<String, String>> }
    assertThat(response["app"])
      .isNotNull
      .containsEntry("name", "Hmpps Delius Api")
  }
}
