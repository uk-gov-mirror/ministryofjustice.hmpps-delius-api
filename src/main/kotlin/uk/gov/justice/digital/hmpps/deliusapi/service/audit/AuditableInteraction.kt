package uk.gov.justice.digital.hmpps.deliusapi.service.audit

enum class AuditableInteraction(val code: String) {
  ADD_CONTACT("CLBI003"),
  ADMINISTER_NSI("NIBI009"),
}
