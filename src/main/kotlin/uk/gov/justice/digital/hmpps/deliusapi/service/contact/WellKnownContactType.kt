package uk.gov.justice.digital.hmpps.deliusapi.service.contact

enum class WellKnownContactType(val code: String, val breachType: BreachType? = null) {
  NSI_REFERRAL("NREF"),
  NSI_COMMENCED("NCOM"),
  NSI_TERMINATED("NTER"),
  NSI_TRANSFER("NTRS"),
  REVIEW_ENFORCEMENT_STATUS("ARWS"),
  BREACH_INIT("AIBR", BreachType.START),
  BREACH_CONCLUDED("ABPC", BreachType.END),
  BREACH_PROVEN_COMMITTED_TO_CUSTODY("ABCC", BreachType.END),
  BREACH_PROVEN_FINE("ABCF", BreachType.END),
  BREACH_PROVEN_NO_ACTION("ABNA", BreachType.END),
  BREACH_PROVEN_SDO_IMPOSED("ABSD", BreachType.END),
  BREACH_WITHDRAWN("ABWD", BreachType.END),
  BREACH_NOT_PROVEN("AB NP", BreachType.END),
  BREACH_RESENTENCE("ABPP", BreachType.END),
  BREACH_PRISON_RECALL("ERCL", BreachType.END),
  START_OF_POST_SENTENCE_SUPERVISION("CPSS", BreachType.END),
  RELEASE_FROM_CUSTODY("EREL");

  companion object {
    private fun getByBreachType(type: BreachType) = values().filter { it.breachType == type }
    val BREACH_START_CODES = getByBreachType(BreachType.START).map { it.code }
    val BREACH_END_CODES = getByBreachType(BreachType.END).map { it.code }

    fun getBreachOrNull(code: String) = values().find { it.code == code && it.breachType != null }
  }
}

enum class BreachType {
  START,
  END,
}
