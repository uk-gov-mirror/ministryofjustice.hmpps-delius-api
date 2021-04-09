package uk.gov.justice.digital.hmpps.deliusapi.config

import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewContact
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewNsi
import uk.gov.justice.digital.hmpps.deliusapi.client.model.NewNsiManager
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

typealias ContactSelector = KProperty1<ContactTestsConfiguration, ContactTestConfiguration>

fun EndToEndTestConfiguration.newContact(select: ContactSelector): NewContact {
  val contact = select(contacts)
  return NewContact(
    offenderCrn = offenderCrn,
    type = contact.type,
    outcome = contact.outcome,
    enforcement = contact.enforcement,
    provider = provider,
    team = team,
    staff = staff,
    officeLocation = contact.officeLocation,
    eventId = contact.eventId,
    requirementId = contact.requirementId,
    nsiId = null,
    date = LocalDate.now(),
    startTime = "12:00",
    endTime = "13:00",
    sensitive = false,
    alert = false,
    notes = "Test contact from Delius API",
    description = "Test contact from Delius API",
  )
}

typealias NsiSelector = KProperty1<NsiTestsConfiguration, NsiTestConfiguration>

fun EndToEndTestConfiguration.newNsi(select: NsiSelector): NewNsi {
  val nsi = select(nsis)
  return NewNsi(
    offenderCrn = offenderCrn,
    type = nsi.type,
    subType = nsi.subType,
    outcome = nsi.outcome,
    intendedProvider = provider,
    manager = NewNsiManager(
      provider = provider,
      team = team,
      staff = staff,
    ),
    eventId = nsi.eventId,
    requirementId = nsi.requirementId,
    notes = "Delius API e2e tests",
    status = nsi.status,
    statusDate = LocalDateTime.now(),
    referralDate = LocalDate.now(),
    startDate = LocalDate.now(),
    expectedStartDate = LocalDate.now(),
    expectedEndDate = LocalDate.now().plusDays(7),
  )
}
