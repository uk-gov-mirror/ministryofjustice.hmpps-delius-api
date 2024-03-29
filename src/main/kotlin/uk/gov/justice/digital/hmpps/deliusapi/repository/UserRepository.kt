package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.User
import java.time.LocalDate

@Repository
interface UserRepository : JpaRepository<User, Long> {
  @Query("select u from User u where UPPER(u.distinguishedName) = UPPER(?1) and ( ?2 < u.endDate or u.endDate = null)")
  fun findActiveUserByName(distinguishedName: String, currentDate: LocalDate): User?
}
