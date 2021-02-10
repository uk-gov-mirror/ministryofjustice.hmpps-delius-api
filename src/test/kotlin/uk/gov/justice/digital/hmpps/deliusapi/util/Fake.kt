package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
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
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

object Fake {
  val faker = Faker()
  val mapper = ContactMapper.INSTANCE

  fun zonedDateTime(): ZonedDateTime = faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault())
  fun localDateTime(): LocalDateTime = zonedDateTime().toLocalDateTime()
  fun localDate(): LocalDate = zonedDateTime().toLocalDate()
  fun localTime(): LocalTime = zonedDateTime().toLocalTime()
  fun crn() = "${faker.lorem().fixedString(1)}${faker.number().randomNumber(6, true)}"

  fun offender() = Offender(id = faker.number().randomNumber(), crn = crn())
  fun contactType() = ContactType(id = faker.number().randomNumber(), code = faker.lorem().characters(1, 10))
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
    contactDate = localDate(),
    contactStartTime = localTime(),
    contactEndTime = localTime(),
    alert = faker.bool().bool(),
    sensitive = faker.bool().bool(),
    notes = faker.lorem().paragraph(),
    createdByUserId = faker.number().randomNumber(),
    lastUpdatedUserId = faker.number().randomNumber(),
    partitionAreaId = faker.number().randomNumber(),
    staffEmployeeId = faker.number().randomNumber(),
    teamProviderId = faker.number().randomNumber(),
    createdDateTime = localDateTime(),
    lastUpdatedDateTime = localDateTime(),
    description = faker.company().bs(),
  ).merge(partial)

  fun contact() = contact(null)

  inline fun <reified Partial : Any> contactDto(partial: Partial?) = mapper.toDto(contact()).merge(partial)

  fun contactDto() = contactDto(null)

  inline fun <reified Partial : Any> newContact(partial: Partial?) = mapper.toNew(contactDto()).merge(partial)

  fun newContact() = newContact(null)

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
