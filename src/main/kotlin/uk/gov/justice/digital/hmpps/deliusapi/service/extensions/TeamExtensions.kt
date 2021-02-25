package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Staff
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

fun Team.getStaffOrBadRequest(staffCode: String): Staff =
  this.staff?.find { it.code == staffCode }
    ?: throw BadRequestException("Staff with officer code '$staffCode' does not exist in team '${this.code}'")
