package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

const val UNALLOCATED_TEAM_PROVIDER_CODE_SUFFIX = "UAT"
const val UNALLOCATED_STAFF_CODE_SUFFIX = "U"

const val INACTIVE_TEAM_CODE_SUFFIX = "IAV"
const val INACTIVE_STAFF_CODE_SUFFIX = "U"

fun Provider.getTeamOrBadRequest(teamCode: String): Team =
  teams?.find { it.code == teamCode }
    ?: throw BadRequestException("Team with code '$teamCode' does not exist for provider '$code'")

fun Provider.getUnallocatedTeam(): Team {
  val key = code + UNALLOCATED_TEAM_PROVIDER_CODE_SUFFIX
  return teams?.find { it.code == key }
    ?: throw RuntimeException("Provider '$code' does not have an unallocated team")
}
