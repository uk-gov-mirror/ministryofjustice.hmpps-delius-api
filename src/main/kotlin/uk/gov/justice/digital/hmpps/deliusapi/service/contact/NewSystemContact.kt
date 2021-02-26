package uk.gov.justice.digital.hmpps.deliusapi.service.contact

import java.time.LocalDateTime

data class NewSystemContact(
  val typeId: Long? = null,
  val type: WellKnownContactType? = null,
  val offenderId: Long,
  val nsiId: Long? = null,
  val eventId: Long? = null,
  val providerId: Long,
  val teamId: Long,
  val staffId: Long,
  val timestamp: LocalDateTime,
  val notes: String? = null,
)
