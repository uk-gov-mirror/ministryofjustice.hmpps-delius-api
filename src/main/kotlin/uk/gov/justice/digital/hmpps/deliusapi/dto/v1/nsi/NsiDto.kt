package uk.gov.justice.digital.hmpps.deliusapi.dto.v1.nsi

import java.time.LocalDate
import java.time.LocalDateTime

data class NsiDto(
  /**
   * The NSI ID.
   */
  val id: Long,

  /**
   * The type of the NSI.
   */
  val type: String,

  /**
   * The sub type of the NSI.
   * This is required for some NSI types.
   */
  val subType: String? = null,

  /**
   * The offender CRN.
   */
  val offenderCrn: String,

  /**
   * An optional event ID that the new NSI will be associated to.
   */
  val eventId: Long? = null,

  /**
   * An optional requirement ID that the new NSI will be associated to.
   * An event is required for association to a requirement.
   */
  val requirementId: Long? = null,

  /**
   * The date of the referral.
   */
  val referralDate: LocalDate,

  /**
   * The expected intervention start date.
   */
  val expectedStartDate: LocalDate?,

  /**
   * The expected intervention end date.
   */
  val expectedEndDate: LocalDate?,

  /**
   * The actual intervention start date.
   */
  val startDate: LocalDate?,

  /**
   * The actual intervention end date.
   * An end date is required if outcome is provided.
   */
  val endDate: LocalDate?,

  /**
   * The length of the intervention.
   * The units of this are determined by the NSI type selected.
   */
  val length: Long?,

  /**
   * The status of the intervention.
   */
  val status: String,

  /**
   * The status date.
   */
  val statusDate: LocalDateTime,

  /**
   * The outcome of the intervention
   */
  val outcome: String?,

  /**
   * General notes.
   */
  val notes: String?,

  /**
   * Intended provider
   */
  val intendedProvider: String,

  /**
   * The active manager assigned to this NSI.
   */
  val manager: NsiManagerDto
)
