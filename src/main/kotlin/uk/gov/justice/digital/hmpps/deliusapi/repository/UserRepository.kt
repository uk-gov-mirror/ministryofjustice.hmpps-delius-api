package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.User

@Repository
interface UserRepository : JpaRepository<User, Long> {
  fun findByDistinguishedName(distinguishedName: String): User?
}

fun UserRepository.findByDistinguishedNameOrThrow(distinguishedName: String) =
  this.findByDistinguishedName(distinguishedName) ?: throw RuntimeException("User with name '$distinguishedName' does not exist")
