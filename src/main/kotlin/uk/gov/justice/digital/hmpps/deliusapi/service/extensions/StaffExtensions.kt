package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff

fun Staff.isUnallocated() = code.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX)

fun Staff.isInactive() = code.endsWith(INACTIVE_TEAM_CODE_SUFFIX + INACTIVE_STAFF_CODE_SUFFIX)

fun Staff.getDisplayName(): String {
  if (isInactive()) {
    return "Inactive"
  }

  if (isUnallocated()) {
    return "Unallocated"
  }

  return listOfNotNull(firstName, middleName, lastName).joinToString(" ")
}

fun staffCodeOrUnallocated(code: String) = if (code.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX)) null else code
