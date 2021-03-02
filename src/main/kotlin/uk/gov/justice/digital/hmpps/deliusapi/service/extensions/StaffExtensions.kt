package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff

fun Staff.isUnallocated() = code.endsWith(UNALLOCATED_STAFF_TEAM_CODE_POSTFIX)
