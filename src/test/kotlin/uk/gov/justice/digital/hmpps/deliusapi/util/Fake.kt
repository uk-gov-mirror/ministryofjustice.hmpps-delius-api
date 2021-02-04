package uk.gov.justice.digital.hmpps.deliusapi.util

import com.github.javafaker.Faker
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.Contact
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NewContact
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

object Fake {
  val faker = Faker()

  fun localDate(): LocalDate = faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
  fun localTime(): LocalTime = faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()

  inline fun <reified Partial : Any> newContact(partial: Partial?) = NewContact(
    offenderId = faker.number().numberBetween(1, 100),
    contactType = faker.lorem().characters(1, 10),
    contactOutcome = faker.lorem().characters(1, 10),
    provider = faker.lorem().characters(3),
    team = faker.lorem().characters(6),
    staff = faker.lorem().characters(7),
    officeLocation = faker.lorem().characters(7),
    contactDate = localDate(),
    contactStartTime = localTime(),
    contactEndTime = localTime(),
    alert = faker.bool().bool(),
    sensitiveContact = faker.bool().bool(),
    notes = faker.lorem().paragraph(),
    contactShortDescription = faker.company().bs(),
  ).merge(partial)

  fun newContact() = newContact(null)

  inline fun <reified Partial : Any> contact(partial: Partial?) = Contact(
    id = faker.number().numberBetween(1, 100),
    offenderId = faker.number().numberBetween(1, 100),
    contactType = faker.lorem().characters(1, 10),
    contactOutcome = faker.lorem().characters(1, 10),
    provider = faker.lorem().characters(3),
    team = faker.lorem().characters(6),
    staff = faker.lorem().characters(7),
    officeLocation = faker.lorem().characters(7),
    contactDate = localDate(),
    contactStartTime = localTime(),
    contactEndTime = localTime(),
    alert = faker.bool().bool(),
    sensitiveContact = faker.bool().bool(),
    notes = faker.lorem().paragraph(),
    contactShortDescription = faker.company().bs(),
  ).merge(partial)

  fun contact() = contact(null)

  /**
   * Merge all properties of partial into target.
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
