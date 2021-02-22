package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.ContactDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewNsiManager
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiManagerDto
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.BusinessInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Disposal
import uk.gov.justice.digital.hmpps.deliusapi.entity.Event
import uk.gov.justice.digital.hmpps.deliusapi.entity.Nsi
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiManager
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiStatus
import uk.gov.justice.digital.hmpps.deliusapi.entity.NsiType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.ReferenceDataMaster
import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.StandardReference
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.entity.TransferReason
import uk.gov.justice.digital.hmpps.deliusapi.entity.YesNoBoth.Y
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import uk.gov.justice.digital.hmpps.deliusapi.mapper.NsiMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.concurrent.TimeUnit

object Fake {
  val faker = Faker()
  val contactMapper: ContactMapper = ContactMapper.INSTANCE
  val nsiMapper: NsiMapper = NsiMapper.INSTANCE

  const val ALLOWED_CONTACT_TYPES = "TST01,TST02,TST03"
  val allowedContactTypes = ALLOWED_CONTACT_TYPES.split(',').toTypedArray()

  private fun Date.toLocalTime(): LocalTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
  private fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  private fun Date.toLocalDateTime(): LocalDateTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

  fun localTimeBetween(hourFrom: Int, hourTo: Int): LocalTime = faker.date().between(
    Date.from(LocalDateTime.of(1900, 1, 1, hourFrom, 0).toInstant(ZoneOffset.UTC)),
    Date.from(LocalDateTime.of(1900, 1, 1, hourTo, 0).toInstant(ZoneOffset.UTC))
  ).toLocalTime()

  fun randomPastLocalDate(): LocalDate = faker.date().past(10, 1, TimeUnit.DAYS).toLocalDate()
  fun randomLocalDateTime(): LocalDateTime = faker.date().past(10, 1, TimeUnit.DAYS).toLocalDateTime()
  fun randomFutureLocalDate(): LocalDate = faker.date().future(10, 1, TimeUnit.DAYS).toLocalDate()

  fun id(): Long = faker.number().numberBetween(1L, 900_000_000_000_000_000L) // maxvalue of db sequences

  private fun crn() = "${faker.lorem().fixedString(1)}${faker.number().randomNumber(6, true)}"

  fun offender() = Offender(id = id(), crn = crn(), events = listOf(event()))

  fun contactType() = ContactType(
    id = id(),
    code = faker.options().option(*allowedContactTypes),
    alertFlag = true,
    outcomeFlag = Y,
    locationFlag = Y,
    attendanceContact = true,
    recordedHoursCredited = true,
    outcomeTypes = listOf(contactOutcomeType()),
  )
  fun contactOutcomeType() = ContactOutcomeType(id = id(), code = faker.lorem().characters(1, 10), compliantAcceptable = true, attendance = true)
  fun provider() = Provider(id = id(), code = faker.lorem().characters(3), teams = listOf(team()))
  fun team() = Team(
    id = id(),
    code = faker.lorem().characters(6),
    staff = listOf(staff()),
    officeLocations = listOf(officeLocation())
  )
  fun officeLocation() = OfficeLocation(id = id(), code = faker.lorem().characters(7))
  fun staff() = Staff(id = id(), code = faker.lorem().characters(7))
  fun requirement() = Requirement(id = id(), offenderId = id(), active = true)
  fun disposal() = Disposal(id = id(), requirements = listOf(requirement()))
  fun event() = Event(id = id(), disposals = listOf(disposal()), referralDate = randomPastLocalDate(), active = true)

  fun contact(): Contact {
    val contactOutcomeType = contactOutcomeType()
    val team = team()
    val provider = provider().copy(teams = listOf(team))
    return Contact(
      id = id(),
      offender = offender(),
      type = contactType().copy(outcomeTypes = listOf(contactOutcomeType)),
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
      partitionAreaId = id(),
      staffEmployeeId = id(),
      teamProviderId = id(),
      description = faker.company().bs(),
      event = event(),
      requirement = requirement(),
      createdDateTime = randomLocalDateTime(),
      lastUpdatedDateTime = randomLocalDateTime(),
      createdByUserId = id(),
      lastUpdatedUserId = id(),
    )
  }

  fun contactDto(): ContactDto = contactMapper.toDto(contact())

  fun newContact(): NewContact = contactMapper.toNew(contactDto())

  fun auditedInteraction() = AuditedInteraction(
    dateTime = randomLocalDateTime(),
    success = faker.bool().bool(),
    parameters = faker.lorem().characters(),
    businessInteraction = businessInteraction(),
    userId = id(),
  )

  fun businessInteraction() = BusinessInteraction(
    id = id(),
    code = faker.letterify("????"),
    description = faker.lorem().characters(50),
    enabledDate = randomLocalDateTime()
  )

  fun nsiType() = NsiType(
    id = id(),
    code = faker.lorem().characters(1, 20),
    offenderLevel = true,
    eventLevel = true,
    allowActiveDuplicates = true,
    allowInactiveDuplicates = true,
    units = standardReference(),
    minimumLength = faker.number().numberBetween(1L, 25L),
    maximumLength = faker.number().numberBetween(75L, 100L),
  )

  fun standardReference() = StandardReference(
    id = id(),
    code = faker.lorem().characters(1, 20),
  )

  fun nsiStatus() = NsiStatus(
    id = id(),
    code = faker.lorem().characters(1, 20),
    contactType = contactType(),
  )

  fun nsiManager(): NsiManager {
    val team = team()
    val provider = provider().copy(teams = listOf(team))
    return NsiManager(
      id = id(),
      startDate = randomPastLocalDate(),
      provider = provider,
      team = team,
      staff = staff(),
      active = true,
      createdDateTime = randomLocalDateTime(),
      lastUpdatedDateTime = randomLocalDateTime(),
      createdByUserId = id(),
      lastUpdatedUserId = id(),
    )
  }

  fun nsiManagerDto(): NsiManagerDto = nsiMapper.toDto(nsiManager())

  fun newNsiManager(): NewNsiManager = nsiMapper.toNew(nsiManagerDto())

  fun nsi() = Nsi(
    id = id(),
    offender = offender(),
    event = event(),
    type = nsiType(),
    subType = standardReference(),
    length = faker.number().numberBetween(25L, 75L),
    referralDate = faker.date().past(100, 20, TimeUnit.DAYS).toLocalDate(),
    expectedStartDate = randomPastLocalDate(),
    expectedEndDate = randomFutureLocalDate(),
    startDate = randomPastLocalDate(),
    endDate = LocalDate.now(),
    status = nsiStatus(),
    statusDate = randomLocalDateTime(),
    notes = faker.lorem().paragraph(),
    outcome = standardReference(),
    active = false, // end date is provided here
    pendingTransfer = false,
    requirement = requirement(),
    intendedProvider = provider(),
    createdDateTime = randomLocalDateTime(),
    lastUpdatedDateTime = randomLocalDateTime(),
    createdByUserId = id(),
    lastUpdatedUserId = id(),
    managers = listOf(nsiManager()),
  )

  fun nsiDto(): NsiDto = nsiMapper.toDto(nsi())

  fun newNsi(): NewNsi = nsiMapper.toNew(nsiDto())

  fun transferReason() = TransferReason(
    id = id(),
    code = faker.lorem().characters(1, 20),
  )

  fun referenceDataMaster() = ReferenceDataMaster(
    id = id(),
    code = faker.lorem().characters(1, 20),
    standardReferences = listOf(standardReference(), standardReference()),
  )
}
