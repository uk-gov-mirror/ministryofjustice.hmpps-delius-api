package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType

@Repository
interface ContactTypeRepository : JpaRepository<ContactType, Long> {
  fun findByCode(code: String): ContactType?
}
