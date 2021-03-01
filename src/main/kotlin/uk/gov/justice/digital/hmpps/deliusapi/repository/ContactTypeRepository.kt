package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.ContactType

@Repository
interface ContactTypeRepository : JpaRepository<ContactType, Long> {
  @Query("select t from ContactType t where t.code = ?1 and (t.selectable = true or t.spgOverride = true)")
  fun findSelectableByCode(code: String): ContactType?

  fun findByCode(code: String): ContactType?
}
