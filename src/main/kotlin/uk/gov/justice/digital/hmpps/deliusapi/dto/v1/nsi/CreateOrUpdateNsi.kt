package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi

import java.time.LocalDate
import java.time.LocalDateTime

interface CreateOrUpdateNsi {
  val referralDate: LocalDate
  val expectedStartDate: LocalDate?
  val expectedEndDate: LocalDate?
  val startDate: LocalDate?
  val endDate: LocalDate?
  val length: Long?
  val status: String
  val statusDate: LocalDateTime
  val outcome: String?
  val notes: String?
  val manager: CreateOrUpdateNsiManager
}
