package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Requirement

fun Requirement.isRehabilitationActivityRequirement() = typeCategory?.code == "F"
