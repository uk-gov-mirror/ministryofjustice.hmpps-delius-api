package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.CreateOrUpdateNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.NsiManagerRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ProviderRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.ReferenceDataMasterRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.TransferReasonRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByCodeOrThrow
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.NewSystemContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.SystemContactService
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getDisplayName
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getStaffOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getTeamOrBadRequest
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getUnallocatedStaff
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getUnallocatedTeam
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.transaction.Transactional

@Service
class NsiManagerService(
  private val repository: NsiManagerRepository,
  private val providerRepository: ProviderRepository,
  private val transferReasonRepository: TransferReasonRepository,
  private val referenceDataMasterRepository: ReferenceDataMasterRepository,
  private val systemContactService: SystemContactService,
) {

  fun createNsiManager(nsi: Nsi, request: NewNsiManager, startDate: LocalDate) {
    val provider = providerRepository.findByCodeAndSelectableIsTrue(request.provider)
      ?: throw BadRequestException("Provider with code '${request.provider}' does not exist")

    val (team, staff) = getTeamStaff(request, provider)
    val allocationReason = referenceDataMasterRepository
      .findByCodeOrThrow(WellKnownReferenceDataset.NSI_MANAGER_ALLOCATION_REASON.code)
      .standardReferences?.find { it.code == "IN1" }
      ?: throw RuntimeException("Cannot find NSI allocation reason with code 'IN1'")

    val transferReason = transferReasonRepository.findByCode(WellKnownTransferReason.NSI.code)
      ?: throw RuntimeException("Cannot find NSI transfer reason")

    nsi.manager = NsiManager(
      nsi = nsi,
      startDate = startDate,
      provider = provider,
      team = team,
      staff = staff,
      transferReason = transferReason,
      allocationReason = allocationReason,
    )
  }

  @Transactional(Transactional.TxType.MANDATORY)
  fun updateNsiManager(nsi: Nsi, request: UpdateNsiManager) {
    val oldManager = nsi.manager
      ?: throw RuntimeException("NSI with id ${nsi.id} has no manager to update")

    val provider = oldManager.provider
      ?: throw RuntimeException("Cannot update manager on NSI with id ${nsi.id} as no manager provider set")

    val (team, staff) = getTeamStaff(request, provider)
    if (staff.id == oldManager.staff?.id && team.id == oldManager.team?.id) {
      // nothing to update
      return
    }

    if (request.transferReason == null) {
      throw BadRequestException("Transfer reason is required to update an NSI manager")
    }

    if (request.transferDate == null) {
      throw BadRequestException("Transfer date is required to update an NSI manager")
    }

    val allocationReason = referenceDataMasterRepository
      .findByCodeOrThrow(WellKnownReferenceDataset.NSI_MANAGER_ALLOCATION_REASON.code)
      .standardReferences?.find { it.code == request.transferReason }
      ?: throw BadRequestException("Cannot find NSI allocation reason with code '${request.transferReason}'")

    val transferReason = transferReasonRepository.findByCode(WellKnownTransferReason.NSI.code)
      ?: throw RuntimeException("Cannot find NSI transfer reason")

    // Set the previous manager as inactive -> this is why we require an existing transaction
    oldManager.endDate = request.transferDate
    oldManager.active = false
    repository.saveAndFlush(oldManager)

    nsi.manager = NsiManager(
      nsi = nsi,
      startDate = request.transferDate,
      provider = provider,
      team = team,
      staff = staff,
      transferReason = transferReason,
      allocationReason = allocationReason,
    )

    systemContactService.createSystemContact(
      NewSystemContact(
        type = WellKnownContactType.NSI_TRANSFER,
        offenderId = nsi.offender?.id!!,
        date = request.transferDate,
        eventId = nsi.event?.id,
        nsiId = nsi.id,
        teamId = team.id,
        staffId = staff.id,
        providerId = provider.id,
        notes = listOfNotNull(
          "Transfer Reason: Internal Transfer",
          "Transfer Date: ${request.transferDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
          "From Trust: ${provider.description}",
          if (oldManager.team == null) null else "From Team: ${oldManager.team!!.description}",
          if (oldManager.staff == null) null else "From Officer: ${oldManager.staff!!.getDisplayName()}",
          "-------------------------------",
          "", // <- ends in an empty newline
        ).joinToString("\n"),
      )
    )
  }

  private fun getTeamStaff(request: CreateOrUpdateNsiManager, provider: Provider): Pair<Team, Staff> {
    val team = if (request.team == null) provider.getUnallocatedTeam()
    else provider.getTeamOrBadRequest(request.team!!)

    val staff = if (request.staff == null) team.getUnallocatedStaff()
    else team.getStaffOrBadRequest(request.staff!!)

    return team to staff
  }
}
