package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Provider
import uk.gov.justice.digital.hmpps.deliusapi.entity.Team
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException

fun Provider.getTeamOrBadRequest(teamCode: String): Team =
  this.teams?.find { it.code == teamCode }
    ?: throw BadRequestException("Team with code '$teamCode' does not exist for provider '${this.code}'")
