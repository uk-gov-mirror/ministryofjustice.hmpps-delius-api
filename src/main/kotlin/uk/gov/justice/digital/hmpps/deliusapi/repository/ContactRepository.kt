package uk.gov.justice.digital.hmpps.deliusapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface ContactRepository : JpaRepository<Contact, Long> {
  fun findAllByNsiId(nsiId: Long): List<Contact>

  @Query(
    "select c from Contact c where c.offender.id = :offenderId and c.type.attendanceContact = true " +
      "and c.date = :date and c.startTime <= :endTime and c.endTime >= :startTime"
  )
  fun findClashingAttendanceContacts(
    @Param("offenderId") offenderId: Long,
    @Param("date") date: LocalDate,
    @Param("startTime") startTime: LocalTime,
    @Param("endTime") endTime: LocalTime,
  ): List<Contact>
}
