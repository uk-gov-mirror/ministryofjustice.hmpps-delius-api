package uk.gov.justice.digital.hmpps.deliusapi.integration.v1.nsi

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.deliusapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ActiveProfiles("test-h2")
class CreateNsiTest : IntegrationTestBase() {
  @Test
  fun `Creating nsi`() {
    val userId = Fake.faker.number().randomNumber()
    val token = jwtAuthHelper.createJwt("bob", userId = userId)
    val newNsi = Fake.newNsi()
    webTestClient.post()
      .uri("/v1/nsi")
      .header("Authorization", "Bearer $token")
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(newNsi)
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
  }
}
