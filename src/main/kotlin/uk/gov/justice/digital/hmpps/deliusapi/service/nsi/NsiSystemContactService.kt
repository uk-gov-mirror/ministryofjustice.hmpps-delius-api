package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi.UpdateNsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.NewSystemContact
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.SystemContactService
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Convenience service for handling the (many) system contacts generated when maintaining NSIs.
 * Note: All contacts created in this service should be rolled back if primary entity cannot be saved.
 *       To enforce this, all operations require an existing transaction.
 */
@Transactional(propagation = Propagation.MANDATORY)
@Service
class NsiSystemContactService(
  private val contactRepository: ContactRepository,
  private val systemContactService: SystemContactService,
) {

  fun createReferralContact(nsi: Nsi) = createWellKnownContact(nsi, WellKnownContactType.NSI_REFERRAL, nsi.referralDate)

  fun createStatusContact(nsi: Nsi) = createStatusContact(nsi, nsi.status!!, nsi.statusDate)

  fun updateStatusContact(nsi: Nsi, newStatus: NsiStatus, request: UpdateNsi) {
    if (nsi.status?.id != newStatus.id) {
      // create new status
      createStatusContact(nsi, newStatus, request.statusDate)
    } else if (nsi.statusDate != request.statusDate) {
      // status unchanged but status date updated
      val existing = contactRepository.findAllByNsiIdAndTypeIdAndDate(
        nsi.id, newStatus.contactTypeId, nsi.statusDate.toLocalDate()
      )
      for (contact in existing) {
        contact.date = request.statusDate.toLocalDate()
        contact.startTime = request.statusDate.toLocalTime()
        contactRepository.saveAndFlush(contact)
      }
    }
  }

  fun createCommencedContact(nsi: Nsi) {
    if (nsi.startDate == null) {
      // the nsi is not yet commenced
      return
    }
    createWellKnownContact(nsi, WellKnownContactType.NSI_COMMENCED, nsi.startDate!!)
  }

  fun updateCommencedContact(nsi: Nsi, request: UpdateNsi) {
    when {
      nsi.startDate == null && request.startDate != null ->
        // nsi is now commenced
        createWellKnownContact(nsi, WellKnownContactType.NSI_COMMENCED, request.startDate)
      nsi.startDate != null && request.startDate == null -> {
        // delete commencement contact
        val existing = contactRepository.findAllByNsiIdAndTypeCode(nsi.id, WellKnownContactType.NSI_COMMENCED.code)
        for (contact in existing) {
          systemContactService.safeDeleteSystemContact(contact)
        }
      }
      nsi.startDate != null && request.startDate != null && nsi.startDate != request.startDate -> {
        // update commencement contact(s)
        val existing = contactRepository.findAllByNsiIdAndTypeCode(nsi.id, WellKnownContactType.NSI_COMMENCED.code)
        for (contact in existing) {
          contact.date = request.startDate
          contactRepository.saveAndFlush(contact)
        }
      }
    }
  }

  fun createTerminationContact(nsi: Nsi) {
    if (!nsi.hasOutcome()) {
      return
    }

    createWellKnownContact(
      nsi, WellKnownContactType.NSI_TERMINATED, nsi.endDate!!,
      notes = nsi.outcome!!.getOutcomeNotes()
    )
  }

  fun updateTerminationContact(nsi: Nsi, newOutcome: StandardReference?, request: UpdateNsi) {
    val hadOutcome = nsi.hasOutcome()
    val willHaveOutcome = newOutcome != null && request.endDate != null
    when {
      !hadOutcome && willHaveOutcome ->
        // create termination contact
        createWellKnownContact(
          nsi, WellKnownContactType.NSI_TERMINATED, request.endDate!!,
          notes = newOutcome!!.getOutcomeNotes()
        )
      hadOutcome && willHaveOutcome && nsi.outcome!!.code != newOutcome!!.code -> {
        // update termination contact(s)
        val existing = contactRepository.findAllByNsiIdAndTypeCode(nsi.id, WellKnownContactType.NSI_TERMINATED.code)
        for (contact in existing) {
          contact.date = request.endDate!!
          contact.notes = newOutcome.getOutcomeNotes()
          contactRepository.saveAndFlush(contact)
        }
      }
    }
  }

  private fun StandardReference.getOutcomeNotes() = "NSI Terminated with Outcome: $description"

  private fun Nsi.hasOutcome() = outcome != null && endDate != null

  private fun createStatusContact(nsi: Nsi, status: NsiStatus, date: LocalDateTime) =
    systemContactService.createSystemContact(
      NewSystemContact(
        typeId = status.contactTypeId,
        offenderId = nsi.offender?.id!!,
        nsiId = nsi.id,
        eventId = nsi.event?.id,
        providerId = nsi.manager?.provider?.id!!,
        teamId = nsi.manager?.team?.id!!,
        staffId = nsi.manager?.staff?.id!!,
        date = date.toLocalDate(),
        startTime = date.toLocalTime(),
      )
    )

  private fun createWellKnownContact(
    nsi: Nsi,
    type: WellKnownContactType,
    date: LocalDate,
    startTime: LocalTime? = null,
    notes: String? = null
  ) =
    systemContactService.createSystemContact(
      NewSystemContact(
        type = type,
        offenderId = nsi.offender?.id!!,
        nsiId = nsi.id,
        eventId = nsi.event?.id,
        providerId = nsi.manager?.provider?.id!!,
        teamId = nsi.manager?.team?.id!!,
        staffId = nsi.manager?.staff?.id!!,
        date = date,
        startTime = startTime,
        notes = notes,
      )
    )
}
