package uk.gov.justice.digital.hmpps.deliusapi.config

import org.springframework.test.context.ActiveProfilesResolver

class EndToEndTestActiveProfilesResolver : ActiveProfilesResolver {
  override fun resolve(testClass: Class<*>) =
    System.getenv("SPRING_PROFILES_ACTIVE")?.split(',', ' ')?.toTypedArray()
      ?: emptyArray()
}
