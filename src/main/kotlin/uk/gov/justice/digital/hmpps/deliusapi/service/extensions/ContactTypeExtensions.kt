package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.BreachType
import uk.gov.justice.digital.hmpps.deliusapi.service.contact.WellKnownContactType

fun ContactType.getBreachType(): BreachType? = WellKnownContactType.getBreachOrNull(code)?.breachType
