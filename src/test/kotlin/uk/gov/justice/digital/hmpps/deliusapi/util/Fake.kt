package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.BusinessInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Disposal
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.Y
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.concurrent.TimeUnit

object Fake {
  val faker = Faker()
  val mapper: ContactMapper = ContactMapper.INSTANCE

  private fun Date.toLocalTime(): LocalTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
  private fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  private fun Date.toLocalDateTime(): LocalDateTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

  fun localTimeBetween(hourFrom: Int, hourTo: Int): LocalTime = faker.date().between(
    Date.from(LocalDateTime.of(1900, 1, 1, hourFrom, 0).toInstant(ZoneOffset.UTC)),
    Date.from(LocalDateTime.of(1900, 1, 1, hourTo, 0).toInstant(ZoneOffset.UTC))
  ).toLocalTime()

  fun randomPastLocalDate(): LocalDate = faker.date().past(10, 1, TimeUnit.DAYS).toLocalDate()
  fun randomLocalDateTime(): LocalDateTime = faker.date().past(10, TimeUnit.DAYS).toLocalDateTime()
  fun randomFutureLocalDate(): LocalDate = faker.date().future(10, TimeUnit.DAYS).toLocalDate()

  private fun crn() = "${faker.lorem().fixedString(1)}${faker.number().randomNumber(6, true)}"

  fun offender(id: Long? = null, events: List<Event> = listOf(event())) =
    Offender(id = id ?: faker.number().randomNumber(), crn = crn(), events = events)

  fun contactType(outcomeTypes: List<ContactOutcomeType> = listOf(contactOutcomeType())) = ContactType(
    id = faker.number().randomNumber(),
    code = faker.lorem().characters(1, 10),
    alertFlag = true,
    outcomeFlag = Y,
    locationFlag = Y,
    attendanceContact = true,
    recordedHoursCredited = true,
    outcomeTypes = outcomeTypes,
  )
  fun contactOutcomeType(code: String? = null) = ContactOutcomeType(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(1, 10), compliantAcceptable = true, attendance = true)
  fun provider(code: String? = null, teams: List<Team> = listOf(team())) =
    Provider(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(3), teams = teams)
  fun team(code: String? = null, staff: List<Staff>? = listOf(staff()), officeLocation: List<OfficeLocation> = listOf(officeLocation())) =
    Team(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(6), staff = staff, officeLocations = officeLocation)
  fun officeLocation(code: String? = null) =
    OfficeLocation(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(7))
  fun staff(code: String? = null) = Staff(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(7))
  fun requirement(id: Long? = null, offenderId: Long? = null) = Requirement(id = id ?: faker.number().randomNumber(), offenderId = offenderId ?: faker.number().randomNumber())
  fun disposal(requirements: List<Requirement>? = listOf(requirement())) = Disposal(id = faker.number().randomNumber(), requirements = requirements)
  fun event(id: Long? = null, disposals: List<Disposal>? = listOf(disposal())) =
    Event(id = id ?: faker.number().randomNumber(), disposals = disposals)

  fun contact(): Contact {
    val contactOutcomeType = contactOutcomeType()
    val team = team()
    val provider = provider(teams = listOf(team))
    return Contact(
      id = faker.number().randomNumber(),
      offender = offender(),
      type = contactType(listOf(contactOutcomeType)),
      outcome = contactOutcomeType,
      provider = provider,
      team = team,
      staff = staff(),
      officeLocation = officeLocation(),
      date = randomPastLocalDate(),
      startTime = localTimeBetween(0, 12),
      endTime = localTimeBetween(12, 23),
      alert = faker.bool().bool(),
      sensitive = faker.bool().bool(),
      notes = faker.lorem().paragraph(),
      createdByUserId = faker.number().randomNumber(),
      lastUpdatedUserId = faker.number().randomNumber(),
      partitionAreaId = faker.number().randomNumber(),
      staffEmployeeId = faker.number().randomNumber(),
      teamProviderId = faker.number().randomNumber(),
      createdDateTime = randomLocalDateTime(),
      lastUpdatedDateTime = randomLocalDateTime(),
      description = faker.company().bs(),
      event = event(),
      requirement = requirement(),
    )
  }

  fun contactDto(): ContactDto = mapper.toDto(contact())

  fun newContact(): NewContact = mapper.toNew(contactDto())

  fun auditedInteraction() = AuditedInteraction(
    dateTime = randomLocalDateTime(),
    success = faker.bool().bool(),
    parameters = faker.lorem().characters(),
    businessInteraction = businessInteraction(),
    userId = faker.number().randomNumber(),
  )

  fun businessInteraction() = BusinessInteraction(
    id = faker.number().randomNumber(),
    code = faker.letterify("????"),
    description = faker.lorem().characters(50),
    enabledDate = randomLocalDateTime()
  )
}
