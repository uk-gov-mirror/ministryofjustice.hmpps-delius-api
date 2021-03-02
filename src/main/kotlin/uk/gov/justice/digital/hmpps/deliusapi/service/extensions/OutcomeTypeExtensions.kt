package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactOutcomeType

/**
 * Determines whether this contact outcome is the case where the offender did not attend but was for an acceptable reason.
 */
fun ContactOutcomeType.isPermissibleAbsence() =
  attendance == false && compliantAcceptable == true
