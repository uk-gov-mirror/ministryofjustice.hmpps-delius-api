package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.BusinessInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.entity.Offender
import uk.gov.justice.digital.hmpps.deliusapi.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.mapper.ContactMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

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

  fun randomLocalDate(): LocalDate = faker.date().past(10, TimeUnit.DAYS).toLocalDate()
  fun randomLocalDateTime(): LocalDateTime = faker.date().past(10, TimeUnit.DAYS).toLocalDateTime()

  private fun crn() = "${faker.lorem().fixedString(1)}${faker.number().randomNumber(6, true)}"

  fun offender() = Offender(id = faker.number().randomNumber(), crn = crn())
  fun contactType() = ContactType(id = faker.number().randomNumber(), code = faker.lorem().characters(1, 10), contactAlertFlag = true)
  fun contactOutcomeType() = ContactOutcomeType(id = faker.number().randomNumber(), code = faker.lorem().characters(1, 10))
  fun provider(code: String? = null, officeLocations: List<OfficeLocation>? = null) =
    Provider(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(3), officeLocations = officeLocations)
  fun officeLocation(code: String? = null, teams: List<Team>? = null) =
    OfficeLocation(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(7), teams = teams)
  fun team(code: String? = null, staff: List<Staff>? = null) = Team(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(6), staff = staff)
  fun staff(code: String? = null) = Staff(id = faker.number().randomNumber(), code = code ?: faker.lorem().characters(7))

  inline fun <reified Partial : Any> contact(partial: Partial?): Contact = Contact(
    id = faker.number().randomNumber(),
    offender = offender(),
    contactType = contactType(),
    contactOutcomeType = contactOutcomeType(),
    provider = provider(),
    team = team(),
    staff = staff(),
    officeLocation = officeLocation(),
    contactDate = randomLocalDate(),
    contactStartTime = localTimeBetween(0, 12),
    contactEndTime = localTimeBetween(12, 23),
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
  ).merge(partial)

  fun contact() = contact(null)

  inline fun <reified Partial : Any> contactDto(partial: Partial?) = mapper.toDto(contact()).merge(partial)

  fun contactDto() = contactDto(null)

  inline fun <reified Partial : Any> newContact(partial: Partial?) = mapper.toNew(contactDto()).merge(partial)

  fun newContact() = newContact(null)

  inline fun <reified Partial : Any> auditedInteraction(partial: Partial?) = AuditedInteraction(
    dateTime = randomLocalDateTime(),
    outcome = faker.lorem().characters(),
    parameters = faker.lorem().characters(),
    businessInteraction = businessInteraction(),
    userID = faker.number().randomNumber(),
  ).merge(partial)

  fun auditedInteraction() = auditedInteraction(null)

  inline fun <reified Partial : Any> businessInteraction(partial: Partial?): BusinessInteraction = BusinessInteraction(
    id = faker.number().randomNumber(),
    code = faker.letterify("????"),
    description = faker.lorem().characters(50),
    enabledDate = randomLocalDateTime()
  ).merge(partial)

  fun businessInteraction() = businessInteraction(null)
  /**
   * Merge all properties of partial into a shallow copy of target.
   */
  inline fun <reified T : Any, reified Partial : Any> T.merge(partial: Partial?): T {
    if (partial == null) {
      return this
    }

    val props = T::class.declaredMemberProperties.associateBy { it.name }
    val partialProps = Partial::class.declaredMemberProperties.associateBy { it.name }

    val primaryConstructor = T::class.primaryConstructor
      ?: throw IllegalArgumentException("merge type must have a primary constructor")

    val args = primaryConstructor.parameters.associateWith { parameter ->
      when {
        partialProps.containsKey(parameter.name) -> partialProps[parameter.name]?.get(partial)
        props.containsKey(parameter.name) -> props[parameter.name]?.get(this)
        else -> throw IllegalStateException("no declared member property found with name '${parameter.name}'")
      }
    }

    return primaryConstructor.callBy(args)
  }
}
