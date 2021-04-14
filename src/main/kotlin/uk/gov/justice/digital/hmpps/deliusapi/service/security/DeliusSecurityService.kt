package uk.gov.justice.digital.hmpps.deliusapi.service.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.entity.User
import uk.gov.justice.digital.hmpps.deliusapi.repository.UserRepository
import java.time.LocalDate

@Service
class DeliusSecurityService(private val userRepository: UserRepository) {
  fun getUser(username: String): User = userRepository.findActiveUserByName(username, LocalDate.now())
    ?: throw AccessDeniedException("No active user with name '$username'")
}
