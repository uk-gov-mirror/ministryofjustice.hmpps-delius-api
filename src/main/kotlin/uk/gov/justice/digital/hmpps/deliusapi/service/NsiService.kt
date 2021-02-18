package uk.gov.justice.digital.hmpps.deliusapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.advice.Auditable
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.PartitionArea
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction
import java.time.LocalDate

@Service
class NsiService {
  @Auditable(AuditableInteraction.ADMINISTER_NSI)
  fun createNsi(request: NewNsi): NsiDto {
    val audit = AuditContext.get(AuditableInteraction.ADMINISTER_NSI)
    audit.offenderId = 1

    val manager = NsiManager(
      startDate = LocalDate.now(),
      provider = Provider(id = 1, code = request.manager.provider),
      team = Team(id = 1, code = request.manager.team),
      staff = Staff(id = 1, code = request.manager.staff),
      partitionArea = PartitionArea(id = 1, area = "")
    )
    val entity = Nsi(
      id = 123,
      offender = Offender(id = 1, crn = request.offenderCrn),
      event = Event(id = 1),
      type = NsiType(id = 1, code = request.type),
      subType = StandardReference(id = 1, code = ""),
      length = request.length,
      referralDate = request.referralDate,
      expectedStartDate = request.expectedStartDate,
      expectedEndDate = request.expectedEndDate,
      startDate = request.startDate,
      endDate = request.endDate,
      status = NsiStatus(id = 1, code = request.status),
      statusDate = request.statusDate,
      notes = request.notes,
      outcome = StandardReference(id = 1, code = ""),
      requirement = Requirement(id = 1, offenderId = 1),
      intendedProvider = Provider(id = 1, code = request.intendedProvider),
      managers = listOf(manager),
    )

    return NsiMapper.INSTANCE.toDto(entity)
  }
}
