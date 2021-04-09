package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.contact.CreateOrUpdateContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Enforcement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.getDuration
import uk.gov.justice.digital.hmpps.deliusapi.service.extensions.isPermissibleAbsence
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime

@Service
class ContactValidationService(
  private val contactRepository: ContactRepository,
  private val enforcementActionRepository: EnforcementActionRepository
) {
  fun validateContactType(request: CreateOrUpdateContact, type: ContactType) {
    if (request.alert && !type.alertFlag) {
      throw BadRequestException("Contact type '${type.code}' does not support alert")
    }

    if (type.recordedHoursCredited && request.endTime == null) {
      throw BadRequestException("Contact type '${type.code}' requires an end time")
    }
  }

  fun validateOutcomeType(request: CreateOrUpdateContact, type: ContactType): ContactOutcomeType? {
    fun get() = type.outcomeTypes?.find { it.code == request.outcome }
      ?: throw BadRequestException("Contact type with code '${type.code}' does not support outcome code '${request.outcome}'")

    // The cases here seem odd but logic has been ported from Delius where:
    // Y -> Outcome Mandatory (if contact date is in the past)
    // N -> Outcome Optional
    // B -> Outcome Not Allowed
    val outcome = when (type.outcomeFlag) {
      YesNoBoth.Y -> {
        if (request.outcome == null) {
          if (request.date.isBefore(LocalDate.now())) {
            throw BadRequestException("Contact type '${type.code}' requires an outcome type as the contact date is in the past '${request.date}'")
          } else null
        } else get()
      }
      YesNoBoth.N -> if (request.outcome == null) null else get()
      YesNoBoth.B -> if (request.outcome == null) null
      else throw BadRequestException("Contact type '${type.code}' does not support an outcome type")
    }

    if (outcome?.isPermissibleAbsence() == false && request.date.isAfter(LocalDate.now())) {
      throw BadRequestException("Outcome code '${outcome.code}' not a permissible absence - only permissible absences can be recorded for a future attendance")
    }

    return outcome
  }

  fun setOutcomeMeta(contact: Contact) {
    contact.attended = contact.outcome?.attendance
    contact.complied = contact.outcome?.compliantAcceptable

    // If offender has complied and attended (outcome is acceptable) then set any hours credited
    if (contact.type.recordedHoursCredited && contact.attended == true && contact.complied == true) {
      contact.hoursCredited = BigDecimal.valueOf(contact.getDuration().toMinutes())
        .divide(BigDecimal(60))
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()
    } else {
      contact.hoursCredited = null
    }
  }

  fun validateEnforcement(request: CreateOrUpdateContact, outcome: ContactOutcomeType?): Enforcement? {
    if (outcome == null) {
      return if (request.enforcement == null) null
      else throw BadRequestException("Enforcement cannot be provided without an outcome")
    }

    if (outcome.compliantAcceptable != false || outcome.enforceable != true) {
      return if (request.enforcement == null) null
      else throw BadRequestException("Outcome '${outcome.code}' is not non-compliant & enforceable, enforcement action is not supported")
    }

    val enforcement = Enforcement(
      actionTakenDate = LocalDate.now(),
      actionTakenTime = LocalTime.now(),
    )

    if (outcome.actionRequired) {
      if (request.enforcement == null) {
        throw BadRequestException("Outcome '${outcome.code}' requires an enforcement action")
      }
      val action = enforcementActionRepository.findByCode(request.enforcement!!)
        ?: throw BadRequestException("Enforcement action '${request.enforcement}' does not exist")
      enforcement.action = action
      if (action.responseByPeriod != null) {
        enforcement.responseDate = LocalDate.now().plusDays(action.responseByPeriod!!)
      }
    }

    return enforcement
  }

  fun validateOfficeLocation(request: CreateOrUpdateContact, type: ContactType, team: Team): OfficeLocation? {
    fun get() = team.officeLocations?.find { it.code == request.officeLocation }
      ?: throw BadRequestException("Team with code '${request.team}' does not exist at office location '${request.officeLocation}'")

    return when (type.locationFlag) {
      YesNoBoth.Y -> {
        if (request.officeLocation == null) {
          throw BadRequestException("Location is required for contact type '${type.code}'")
        }
        get()
      }
      YesNoBoth.N -> {
        if (request.officeLocation != null) {
          throw BadRequestException("Contact type '${type.code}' does not support a location")
        }
        null
      }
      YesNoBoth.B -> if (request.officeLocation == null) null else get()
    }
  }

  fun validateFutureAppointmentClashes(request: CreateOrUpdateContact, type: ContactType, offender: Offender, existingId: Long? = null) {
    if (!type.attendanceContact || request.endTime == null || !request.date.isAfter(LocalDate.now())) {
      return
    }

    val clashes = contactRepository.findClashingAttendanceContacts(
      offender.id,
      request.date,
      request.startTime,
      request.endTime as LocalTime,
    ).filter { it.id != existingId }

    if (clashes.isNotEmpty()) {
      val ids = clashes.joinToString(", ") { "'${it.id}'" }
      throw BadRequestException(
        "Contact type '${type.code}' is an attendance type so must not clash with any other " +
          "attendance contacts but clashes with contacts with ids $ids"
      )
    }
  }

  fun validateAssociatedEntity(
    type: ContactType,
    requirement: Requirement? = null,
    event: Event? = null,
    nsi: Nsi? = null,
  ) {
    if (nsi != null) {
      // Contact is at nsi level - contact type must support nsi type
      if (type.nsiTypes?.any { it.id == nsi.type.id } != true) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for an NSI with type '${nsi.type.code}'")
      }
    } else if (requirement != null) {
      // Contact is at "Whole Order" level - type must be whole order level OR have matching requirement type category.
      if (!type.wholeOrderLevel && type.requirementTypeCategories?.any { it.id == requirement.typeCategory?.id } != true) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a requirement in category '${requirement.typeCategory?.code}'")
      }
    } else if (event != null) {
      // Contact is at event level - type must support relevant pre/post CJA 2003 status of event.
      // It looks like in Delius that these are not mutually exclusive!
      val isLegacy = event.disposals?.any { it.type?.legacyOrder == true } ?: false
      if (isLegacy && !type.legacyOrderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a pre CJA 2003 event")
      }
      val isCja = event.disposals?.any { it.type?.cja2003Order == true } ?: false
      if (isCja && !type.cjaOrderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate for a CJA 2003 event")
      }
    } else {
      // Contact is at offender level
      if (!type.offenderLevel) {
        throw BadRequestException("Contact type '${type.code}' is not appropriate at offender level")
      }
    }
  }
}
