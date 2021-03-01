package uk.gov.justice.digital.hmpps.deliusapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.lang.RuntimeException

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  protected lateinit var auditedInteractionRepository: AuditedInteractionRepository

  protected var userId = 0L

  @BeforeEach
  fun beforeEach() {
    userId = Fake.faker.number().randomNumber()
  }

  protected fun WebTestClient.RequestBodySpec.whenSendingUnauthenticatedRequest(): WebTestClient.ResponseSpec = this
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue("{}")
    .exchange()

  protected fun WebTestClient.RequestBodySpec.whenSendingMalformedJson(): WebTestClient.ResponseSpec = this
    .havingAuthentication()
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue("{,}")
    .exchange()

  protected fun WebTestClient.BodyContentSpec.shouldReturnJsonParseError() = this
    .jsonPath("$.userMessage").value(startsWith("JSON parse error: "))

  protected fun WebTestClient.BodyContentSpec.shouldReturnValidationError(vararg invalidPaths: String) = this
    .jsonPath("$.userMessage").value(allOf(invalidPaths.map { containsString(it) }))

  protected fun <T : WebTestClient.RequestHeadersSpec<T>> T.havingAuthentication(
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expired: Boolean = false,
    deliusUser: Boolean = true,
  ): T {
    val token = jwtAuthHelper.createJwt(
      "bob",
      userId = userId,
      scope = scope,
      roles = roles,
      expired = expired,
      deliusUser = deliusUser
    )
    return this.header("Authorization", "Bearer $token")
  }

  protected fun shouldNotAudit(interaction: AuditableInteraction) {
    val interactions = auditedInteractionRepository.findAllByUserIdAndBusinessInteractionCode(userId, interaction.code)
    assertThat(interactions).noneMatch {
      !it.success && it.businessInteraction.code == interaction.code
    }
  }

  protected fun shouldAudit(interaction: AuditableInteraction, parameters: Map<String, Any?>) {
    val interactions = auditedInteractionRepository.findAllByUserIdAndBusinessInteractionCode(userId, interaction.code)
    val expected = parameters.entries.map { "${it.key}='${it.value}'" }.toSet()

    assertThat(interactions).anyMatch {
      it.success &&
        it.businessInteraction.code == interaction.code &&
        it.parameters.split(",").map { p -> p.trim() }.toSet().containsAll(expected)
    }
  }

  protected fun <T> WebTestClient.BodyContentSpec.shouldCreateEntityById(repository: CrudRepository<T, Long>, assertion: (entity: T) -> Unit): WebTestClient.BodyContentSpec =
    this.jsonPath("$.id").value<Any> {
      val id = when (it) {
        is Long -> it
        is Int -> it.toLong()
        else -> throw RuntimeException("unknown id type ${it.javaClass.name}")
      }

      val entity = repository.findByIdOrNull(id)
      assertThat(entity).describedAs("should save entity").isNotNull

      assertion(entity!!)
    }
}
