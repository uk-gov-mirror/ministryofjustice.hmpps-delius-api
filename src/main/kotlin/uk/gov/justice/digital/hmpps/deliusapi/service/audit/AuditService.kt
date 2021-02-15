package uk.gov.justice.digital.hmpps.deliusapi.service.audit

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.BusinessInteractionRepository
import java.time.LocalDateTime

@Service
class AuditService(
  private val auditedInteractionRepository: AuditedInteractionRepository,
  private val businessInteractionRepository: BusinessInteractionRepository
) {

  fun successfulInteraction(userId: Long, interaction: AuditableInteraction, offenderId: Long = -1, nsiId: Long = -1) =
    createAuditedInteraction(userId, interaction, true, offenderId, nsiId)

  fun failedInteraction(userId: Long, interaction: AuditableInteraction, offenderId: Long = -1, nsiId: Long = -1) =
    createAuditedInteraction(userId, interaction, false, offenderId, nsiId)

  fun createAuditedInteraction(
    userId: Long,
    interaction: AuditableInteraction,
    success: Boolean,
    offenderId: Long,
    nsiId: Long
  ) {
    val parameterMap =
      mapOf(AuditParameter.OFFENDER_ID to offenderId, AuditParameter.NSI_ID to nsiId).filter { e -> e.value > 0 }

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
    userID: Long,
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
      userID
    )

    auditedInteractionRepository.saveAndFlush(auditedInteraction)
  }

  private fun formatInteractionParameters(parameters: Map<AuditParameter, String>): String {
    return parameters.map { (key, value) -> "${key.code}='$value'" }.joinToString(", ")
  }

  private fun isInteractionAuditable(enabledDate: LocalDateTime?) =
    enabledDate != null && LocalDateTime.now().isAfter(enabledDate)
}
