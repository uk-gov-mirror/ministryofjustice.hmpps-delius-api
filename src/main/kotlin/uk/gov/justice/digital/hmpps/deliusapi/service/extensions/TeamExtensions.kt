package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

fun Team.getStaffOrBadRequest(staffCode: String): Staff =
  staff?.find { it.code == staffCode }
    ?: throw BadRequestException("Staff with officer code '$staffCode' does not exist in team '$code'")

fun Team.isUnallocated() = code.endsWith(UNALLOCATED_TEAM_PROVIDER_CODE_POSTFIX)

fun Team.getUnallocatedStaff(): Staff {
  val key = (if (code.length > 6) code.substring(0..5) else code) + UNALLOCATED_STAFF_TEAM_CODE_POSTFIX
  return staff?.find { it.code == key }
    ?: throw RuntimeException("Team '$code' does not have an unallocated staff member")
}
