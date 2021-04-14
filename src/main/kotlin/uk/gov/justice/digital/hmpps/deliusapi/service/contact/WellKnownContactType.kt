package uk.gov.justice.digital.hmpps.deliusapi.service.contact

enum class WellKnownContactType(val code: String, val breachType: BreachType? = null, val isAppointment: Boolean = false) {
  NSI_REFERRAL("NREF"),
  NSI_COMMENCED("NCOM"),
  NSI_TERMINATED("NTER"),
  NSI_TRANSFER("NTRS"),
  REVIEW_ENFORCEMENT_STATUS("ARWS"),
  BREACH_INIT("AIBR", breachType = BreachType.START),
  BREACH_CONCLUDED("ABPC", breachType = BreachType.END),
  BREACH_PROVEN_COMMITTED_TO_CUSTODY("ABCC", breachType = BreachType.END),
  BREACH_PROVEN_FINE("ABCF", breachType = BreachType.END),
  BREACH_PROVEN_NO_ACTION("ABNA", breachType = BreachType.END),
  BREACH_PROVEN_SDO_IMPOSED("ABSD", breachType = BreachType.END),
  BREACH_WITHDRAWN("ABWD", breachType = BreachType.END),
  BREACH_NOT_PROVEN("AB NP", breachType = BreachType.END),
  BREACH_RESENTENCE("ABPP", breachType = BreachType.END),
  BREACH_PRISON_RECALL("ERCL", breachType = BreachType.END),
  START_OF_POST_SENTENCE_SUPERVISION("CPSS", breachType = BreachType.END),
  RELEASE_FROM_CUSTODY("EREL"),
  APPOINTMENT("CUPA", isAppointment = true);

  companion object {
    private fun getByBreachType(type: BreachType) = values().filter { it.breachType == type }
    val BREACH_START_CODES = getByBreachType(BreachType.START).map { it.code }
    val BREACH_END_CODES = getByBreachType(BreachType.END).map { it.code }

    fun getOrNull(code: String) = values().find { it.code == code }

    fun getBreachOrNull(code: String) = values().find { it.code == code && it.breachType != null }
  }
}

enum class BreachType {
  START,
  END,
}
