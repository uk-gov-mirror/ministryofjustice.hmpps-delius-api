package uk.gov.justice.digital.hmpps.deliusapi.service.security

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.User
import uk.gov.justice.digital.hmpps.deliusapi.repository.UserRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.findByDistinguishedNameOrThrow

@Service
class DeliusSecurityService(private val userRepository: UserRepository) {
  fun getUser(username: String): User = userRepository.findByDistinguishedNameOrThrow(username)
}
