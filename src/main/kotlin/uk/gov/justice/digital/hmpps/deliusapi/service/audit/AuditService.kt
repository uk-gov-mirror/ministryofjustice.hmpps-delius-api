package uk.gov.justice.digital.hmpps.deliusapi.service.audit

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.SecurityUserContext
import java.time.LocalDateTime

@Service
class AuditService(
  private val auditedInteractionRepository: AuditedInteractionRepository,
  private val businessInteractionRepository: BusinessInteractionRepository,
  private val securityUserContext: SecurityUserContext,
) {

  fun successfulInteraction(interaction: AuditableInteraction, context: AuditContext) =
    createAuditedInteraction(interaction, true, context)

  fun failedInteraction(interaction: AuditableInteraction, context: AuditContext) =
    createAuditedInteraction(interaction, false, context)

  fun createAuditedInteraction(interaction: AuditableInteraction, success: Boolean, context: AuditContext) {
    val parameterMap = mapOf(
      AuditParameter.OFFENDER_ID to (context.offenderId ?: -1),
      AuditParameter.NSI_ID to (context.nsiId ?: -1)
    ).filter { e -> e.value > 0 }

    val userId = securityUserContext.getCurrentDeliusUserId()

    createAuditedInteraction(
      LocalDateTime.now(),
      userId,
      interaction,
      parameterMap.mapValues { it.value.toString() },
      success
    )
  }

  fun createAuditedInteraction(
    dateTime: LocalDateTime = LocalDateTime.now(),
    userId: Long,
    interaction: AuditableInteraction,
    parameters: Map<AuditParameter, String>,
    success: Boolean
  ) {

    if (parameters.isEmpty()) {
      throw BadRequestException("No audit parameters provided")
    }

    val businessInteraction = businessInteractionRepository.findFirstByCode(interaction.code)
      ?: throw BadRequestException("Business Interaction with code ${interaction.code} does not exist")

    if (!isInteractionAuditable(businessInteraction.enabledDate)) {
      return
    }

    val auditedInteraction = AuditedInteraction(
      dateTime,
      success,
      formatInteractionParameters(parameters),
      businessInteraction,
      userId
    )

    auditedInteractionRepository.saveAndFlush(auditedInteraction)
  }

  private fun formatInteractionParameters(parameters: Map<AuditParameter, String>): String =
    parameters.map { (key, value) -> "${key.code}='$value'" }.joinToString(", ")

  private fun isInteractionAuditable(enabledDate: LocalDateTime?) =
    enabledDate != null && LocalDateTime.now().isAfter(enabledDate)
}
