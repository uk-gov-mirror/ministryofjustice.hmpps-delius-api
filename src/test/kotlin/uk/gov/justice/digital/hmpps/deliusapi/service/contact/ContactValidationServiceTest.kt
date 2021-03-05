package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.repository.ContactRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class ContactValidationServiceTest {
  @Mock private lateinit var contactRepository: ContactRepository
  @InjectMocks private lateinit var subject: ContactValidationService

  @Test
  fun `Successfully validating contact type`() =
    attemptingToValidateContactType(success = true)

  @Test
  fun `Attempting to validate non-alert contact type with alert flag set`() =
    attemptingToValidateContactType(success = false, alertFlag = false)

  @Test
  fun `Attempting to validate recorded hours contact type without end time`() =
    attemptingToValidateContactType(success = false, havingEndTime = false)

  @Test
  fun `Successfully validating outcome type`() =
    attemptingToValidateOutcomeType(success = true)

  @Test
  fun `Successfully validating required outcome type without outcome in future`() =
    attemptingToValidateOutcomeType(success = null, havingRequestOutcome = false, havingPastDate = false)

  @Test
  fun `Attempting to validate required outcome type without outcome`() =
    attemptingToValidateOutcomeType(success = false, havingRequestOutcome = false)

  @Test
  fun `Attempting to validate non-permissible absence outcome type in future`() =
    attemptingToValidateOutcomeType(success = false, isPermissibleAbsence = false, havingPastDate = false)

  @Test
  fun `Successfully validating non-permissible absence outcome type in past`() =
    attemptingToValidateOutcomeType(success = true, isPermissibleAbsence = false)

  @Test
  fun `Successfully validating office location`() =
    attemptingToValidateOfficeLocation(success = true)

  @Test
  fun `Attempting to validate required office location without office location`() =
    attemptingToValidateOfficeLocation(success = false, havingRequestOfficeLocation = false)

  @Test
  fun `Attempting to validate required office location with missing office location`() =
    attemptingToValidateOfficeLocation(success = false, havingOfficeLocation = false)

  @Test
  fun `Attempting to validate non-required office location with office location`() =
    attemptingToValidateOfficeLocation(success = false, locationFlag = YesNoBoth.N)

  @Test
  fun `Successfully validating non-required office location without office location`() =
    attemptingToValidateOfficeLocation(success = null, locationFlag = YesNoBoth.N, havingRequestOfficeLocation = false)

  @Test
  fun `Successfully validating required-or-non-required office location with office location`() =
    attemptingToValidateOfficeLocation(success = true, locationFlag = YesNoBoth.B)

  @Test
  fun `Successfully validating required-or-non-required office location without office location`() =
    attemptingToValidateOfficeLocation(success = null, locationFlag = YesNoBoth.B, havingRequestOfficeLocation = false)

  @Test
  fun `Successfully validating future appointment without clashes`() =
    attemptingToValidateFutureAppointmentClashes(success = true, havingClashes = false)

  @Test
  fun `Successfully validating past appointment with clashes`() =
    attemptingToValidateFutureAppointmentClashes(success = true, havingFutureDate = false)

  @Test
  fun `Successfully validating future appointment without end time`() =
    attemptingToValidateFutureAppointmentClashes(success = true, havingEndTime = false)

  @Test
  fun `Successfully validating non-attendance contact as future appointment`() =
    attemptingToValidateFutureAppointmentClashes(success = true, attendanceContact = false)

  @Test
  fun `Attempting to validate future appointment with clashes`() =
    attemptingToValidateFutureAppointmentClashes(success = false, havingClashes = true)

  @Test
  fun `Successfully validating whole order, requirement contact`() {
    val type = Fake.contactType().copy(wholeOrderLevel = true)
    assertDoesNotThrow { subject.validateAssociatedEntity(type, Fake.requirement(), null, null) }
  }

  @Test
  fun `Successfully validating non-whole order, requirement contact with explicit requirement type`() {
    val requirement = Fake.requirement()
    val type = Fake.contactType().copy(
      wholeOrderLevel = false,
      requirementTypeCategories = listOf(requirement.typeCategory!!)
    )
    assertDoesNotThrow { subject.validateAssociatedEntity(type, requirement) }
  }

  @Test
  fun `Attempting to validate invalid requirement contact`() {
    val type = Fake.contactType().copy(wholeOrderLevel = false)
    assertThrows<BadRequestException> { subject.validateAssociatedEntity(type, Fake.requirement()) }
  }

  @Test
  fun `Attempting to validate non-cja 2003 event contact`() {
    val type = Fake.contactType().copy(cjaOrderLevel = false)
    val disposal = Fake.disposal().copy(type = Fake.disposalType().copy(cja2003Order = true))
    assertThrows<BadRequestException> {
      subject.validateAssociatedEntity(type, event = Fake.event().copy(disposals = listOf(disposal)))
    }
  }

  @Test
  fun `Attempting to validate non-legacy event contact`() {
    val type = Fake.contactType().copy(legacyOrderLevel = false)
    val disposal = Fake.disposal().copy(type = Fake.disposalType().copy(legacyOrder = true))
    assertThrows<BadRequestException> {
      subject.validateAssociatedEntity(type, event = Fake.event().copy(disposals = listOf(disposal)))
    }
  }

  @Test
  fun `Successfully validating event contact`() {
    val type = Fake.contactType().copy(legacyOrderLevel = true, cjaOrderLevel = true)
    val disposals = listOf(
      Fake.disposal().copy(type = Fake.disposalType().copy(cja2003Order = true)),
      Fake.disposal().copy(type = Fake.disposalType().copy(legacyOrder = true))
    )
    assertDoesNotThrow {
      subject.validateAssociatedEntity(type, event = Fake.event().copy(disposals = disposals))
    }
  }

  @Test
  fun `Attempting to validate invalid nsi contact`() {
    assertThrows<BadRequestException> { subject.validateAssociatedEntity(Fake.contactType(), nsi = Fake.nsi()) }
  }

  @Test
  fun `Successfully validating nsi contact`() {
    val nsi = Fake.nsi()
    val type = Fake.contactType().copy(nsiTypes = listOf(nsi.type!!))
    assertDoesNotThrow { subject.validateAssociatedEntity(type, nsi = nsi) }
  }

  private fun attemptingToValidateContactType(
    success: Boolean,
    alert: Boolean = true,
    alertFlag: Boolean = true,
    recordedHoursCredited: Boolean = true,
    havingEndTime: Boolean = true,
  ) {
    val request = Fake.newContact().copy(alert = alert, endTime = if (havingEndTime) LocalTime.NOON else null)
    val type = Fake.contactType().copy(alertFlag = alertFlag, recordedHoursCredited = recordedHoursCredited)

    if (success) {
      assertDoesNotThrow { subject.validateContactType(request, type) }
    } else {
      assertThrows<BadRequestException> { subject.validateContactType(request, type) }
    }
  }

  private fun attemptingToValidateOutcomeType(
    success: Boolean?,
    outcomeFlag: YesNoBoth = YesNoBoth.Y,
    havingRequestOutcome: Boolean = true,
    havingPastDate: Boolean = true,
    havingOutcome: Boolean = true,
    isPermissibleAbsence: Boolean = true,
  ) {
    val request = Fake.newContact().copy(
      outcome = if (havingRequestOutcome) "some-outcome" else null,
      date = if (havingPastDate) Fake.randomPastLocalDate() else Fake.randomFutureLocalDate(),
    )
    val outcomeType = Fake.contactOutcomeType().copy(
      code = if (havingOutcome) "some-outcome" else "some-other-outcome",
      attendance = !isPermissibleAbsence,
      compliantAcceptable = isPermissibleAbsence,
    )
    val type = Fake.contactType().copy(
      outcomeFlag = outcomeFlag,
      outcomeTypes = listOf(outcomeType),
    )

    if (success == false) {
      assertThrows<BadRequestException> { subject.validateOutcomeType(request, type) }
    } else {
      val observed = subject.validateOutcomeType(request, type)
      if (success == null) {
        assertThat(observed).isEqualTo(null)
      } else {
        assertThat(observed).isSameAs(outcomeType)
      }
    }
  }

  private fun attemptingToValidateOfficeLocation(
    success: Boolean?,
    locationFlag: YesNoBoth = YesNoBoth.Y,
    havingRequestOfficeLocation: Boolean = true,
    havingOfficeLocation: Boolean = true,
  ) {
    val request = Fake.newContact().copy(
      officeLocation = if (havingRequestOfficeLocation) "some-office-location" else null
    )
    val type = Fake.contactType().copy(locationFlag = locationFlag)
    val officeLocation = Fake.officeLocation().copy(
      code = if (havingOfficeLocation) "some-office-location" else "some-other-office-location"
    )
    val team = Fake.team().copy(
      officeLocations = listOf(officeLocation)
    )

    if (success == false) {
      assertThrows<BadRequestException> { subject.validateOfficeLocation(request, type, team) }
    } else {
      val observed = subject.validateOfficeLocation(request, type, team)
      if (success == null) {
        assertThat(observed).isEqualTo(null)
      } else {
        assertThat(observed).isSameAs(officeLocation)
      }
    }
  }

  private fun attemptingToValidateFutureAppointmentClashes(
    success: Boolean,
    attendanceContact: Boolean = true,
    havingEndTime: Boolean = true,
    havingFutureDate: Boolean = true,
    havingClashes: Boolean? = null,
  ) {
    val request = Fake.newContact().copy(
      endTime = if (havingEndTime) LocalTime.NOON else null,
      date = if (havingFutureDate) Fake.randomFutureLocalDate() else Fake.randomPastLocalDate()
    )
    val type = Fake.contactType().copy(attendanceContact = attendanceContact)
    val offender = Fake.offender()
    val existing = Fake.contact()

    if (havingClashes != null) {
      whenever(
        contactRepository.findClashingAttendanceContacts(
          offender.id,
          request.date,
          request.startTime,
          request.endTime as LocalTime,
        )
      ).thenReturn(if (havingClashes) listOf(existing, Fake.contact()) else listOf(existing))
    }

    if (success) {
      assertDoesNotThrow { subject.validateFutureAppointmentClashes(request, type, offender, existing.id) }
    } else {
      assertThrows<BadRequestException> { subject.validateFutureAppointmentClashes(request, type, offender, existing.id) }
    }
  }
}
