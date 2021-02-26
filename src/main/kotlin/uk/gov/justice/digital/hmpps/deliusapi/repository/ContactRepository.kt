package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact

@Repository
interface ContactRepository : JpaRepository<Contact, Long> {
  fun findAllByNsiId(nsiId: Long): List<Contact>
}
