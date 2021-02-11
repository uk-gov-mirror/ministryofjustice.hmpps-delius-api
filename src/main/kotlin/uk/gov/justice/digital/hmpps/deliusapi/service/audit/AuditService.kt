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

  fun successfulInteraction(userId: Long, offenderId: Long, interaction: AuditableInteraction) =
    createAuditedInteraction(
      LocalDateTime.now(),
      userId,
      interaction,
      mapOf(AuditParameter.OFFENDER_ID to offenderId.toString()),
      true
    )

  fun failedInteraction(userId: Long, offenderId: Long, interaction: AuditableInteraction) =
    createAuditedInteraction(
      LocalDateTime.now(),
      userId,
      interaction,
      mapOf(AuditParameter.OFFENDER_ID to offenderId.toString()),
      false
    )

  fun createAuditedInteraction(
    dateTime: LocalDateTime = LocalDateTime.now(),
    userID: Long,
    interaction: AuditableInteraction,
    parameters: Map<AuditParameter, String>,
    success: Boolean
  ) {
    val businessInteraction = businessInteractionRepository.findByCode(interaction.code)
      ?: throw BadRequestException("Business Interaction with code ${interaction.code} does not exist")

    if (!isInteractionAuditable(businessInteraction.enabledDate)) {
      return
    }

    val auditedInteraction = AuditedInteraction(
      dateTime,
      when {
        success -> INTERACTION_SUCCEEDED
        else -> INTERACTION_FAILED
      },
      formatInteractionParameters(parameters),
      businessInteraction,
      userID
    )

    auditedInteractionRepository.saveAndFlush(auditedInteraction)
  }

  private fun formatInteractionParameters(parameters: Map<AuditParameter, String>): String {
    val builder = StringBuilder()
    parameters.forEach { (key, value) ->
      if (builder.isNotEmpty()) {
        builder.append(",")
      }
      builder.append("${key.code}='$value'")
    }

    return builder.toString()
  }

  private fun isInteractionAuditable(enabledDate: LocalDateTime?) =
    enabledDate != null && LocalDateTime.now().isAfter(enabledDate)

  companion object {
    private const val INTERACTION_SUCCEEDED = "P"
    private const val INTERACTION_FAILED = "F"
  }
}
