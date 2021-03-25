package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import java.time.LocalDate
import java.time.LocalTime

data class NewSystemContact(
  val typeId: Long? = null,
  val type: WellKnownContactType? = null,
  val offenderId: Long,
  val nsiId: Long? = null,
  val eventId: Long? = null,
  val providerId: Long,
  val teamId: Long,
  val staffId: Long,
  val date: LocalDate,
  val startTime: LocalTime? = null,
  val notes: String? = null,
)
