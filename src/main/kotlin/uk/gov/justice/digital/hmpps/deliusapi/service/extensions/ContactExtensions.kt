package uk.gov.justice.digital.hmpps.deliusapi.service.extensions

import uk.gov.justice.digital.hmpps.deliusapi.entity.Contact
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Stack

fun Contact.getDuration(): Duration {
  if (startTime != null && endTime != null) {
    return Duration.between(startTime, endTime)
  }
  return Duration.ZERO
}

// TODO is separator correct?
const val CONTACT_NOTES_SEPARATOR = "\n\n---------\n\n"

fun Contact.updateNotes(vararg sections: String?) {
  notes = listOfNotNull(notes, *sections).joinToString(CONTACT_NOTES_SEPARATOR)
}

fun Contact.getStartDateTime() =
  if (startTime == null) LocalDateTime.of(date, LocalTime.MIDNIGHT)
  else LocalDateTime.of(date, startTime)

fun Contact.flattenLinkedContacts(): List<Contact> {
  // collect the tree of linked contacts, being careful to avoid infinite recursion
  val result = mutableListOf<Contact>()
  val visited = mutableSetOf(id)
  val toVisit = Stack<Contact>()
  toVisit.push(this)

  while (!toVisit.empty()) {
    val current = toVisit.pop()
    val newLinked = current.linkedContacts.filter { !visited.contains(it.id) }

    result.addAll(newLinked)
    visited.addAll(newLinked.map { it.id })
    toVisit.addAll(newLinked)
  }

  return result.toList()
}
