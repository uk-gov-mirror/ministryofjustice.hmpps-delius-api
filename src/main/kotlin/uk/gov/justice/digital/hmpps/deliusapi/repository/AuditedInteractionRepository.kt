package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import java.util.Date

@Repository
interface AuditedInteractionRepository : JpaRepository<AuditedInteraction, Date>
