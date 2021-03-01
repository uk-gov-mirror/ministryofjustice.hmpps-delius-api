package uk.gov.justice.digital.hmpps.deliusapi.service.contact

enum class WellKnownContactType(val code: String) {
  REFERRAL("NREF"),
  COMMENCED("NCOM"),
  TERMINATED("NTER"),
}
