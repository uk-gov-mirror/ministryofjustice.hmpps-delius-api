package uk.gov.justice.digital.hmpps.deliusapi.service.nsi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadRequestException
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class NsiValidationServiceTest {
  @InjectMocks private lateinit var subject: NsiValidationService

  @Test
  fun `Asserting type constraints but active & not allow active duplicates`() {
    val request = Fake.newNsi().copy(endDate = null)
    val type = Fake.nsiType().apply { allowActiveDuplicates = false }
    assertThrows<BadRequestException> { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Asserting type constraints but inactive & not allow inactive duplicates`() {
    val request = Fake.newNsi().copy(endDate = Fake.randomPastLocalDate())
    val type = Fake.nsiType().apply { allowInactiveDuplicates = false }
    assertThrows<BadRequestException> { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Asserting type constraints but length not required and provided`() {
    val request = Fake.newNsi().copy(length = 123)
    val type = Fake.nsiType().apply { units = null }
    assertThrows<BadRequestException> { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Asserting type constraints but length required and not provided`() {
    val request = Fake.newNsi().copy(length = null)
    val type = Fake.nsiType().apply { units = Fake.standardReference() }
    assertThrows<BadRequestException> { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Asserting type constraints but length smaller than minimum`() {
    val request = Fake.newNsi().copy(length = 99)
    val type = Fake.nsiType().apply {
      units = Fake.standardReference()
      minimumLength = 100
      maximumLength = 200
    }
    assertThrows<BadRequestException> { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Asserting type constraints but length larger than maximum`() {
    val request = Fake.newNsi().copy(length = 201)
    val type = Fake.nsiType().apply {
      units = Fake.standardReference()
      minimumLength = 100
      maximumLength = 200
    }
    assertThrows<BadRequestException> { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Successfully asserting type constraints with length`() {
    val request = Fake.newNsi().copy(length = 100)
    val type = Fake.nsiType().apply {
      units = Fake.standardReference()
      minimumLength = 100
      maximumLength = 200
    }
    assertDoesNotThrow { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Successfully asserting type constraints without length`() {
    val request = Fake.newNsi().copy(length = null)
    val type = Fake.nsiType().apply { units = null }
    assertDoesNotThrow { subject.assertTypeConstraints(type, request) }
  }

  @Test
  fun `Validating status but status does not exist`() {
    val request = Fake.newNsi()
    val type = Fake.nsiType()
    assertThrows<BadRequestException> { subject.validateStatus(type, request) }
  }

  @Test
  fun `Successfully validating status`() {
    val request = Fake.newNsi()
    val status = Fake.nsiStatus().apply { code = request.status }
    val type = Fake.nsiType().apply { statuses = listOf(status) }
    val observed = subject.validateStatus(type, request)
    assertThat(observed).isSameAs(status)
  }

  @Test
  fun `Validating outcome but no outcome requested`() {
    val request = Fake.newNsi().copy(outcome = null)
    val type = Fake.nsiType()
    val observed = subject.validateOutcome(type, request)
    assertThat(observed).isEqualTo(null)
  }

  @Test
  fun `Validating outcome but no outcome does not exist`() {
    val request = Fake.newNsi().copy(outcome = "some-missing-outcome")
    val type = Fake.nsiType()
    assertThrows<BadRequestException> { subject.validateOutcome(type, request) }
  }

  @Test
  fun `Successfully validating outcome`() {
    val outcome = Fake.standardReference()
    val request = Fake.newNsi().copy(outcome = outcome.code)
    val type = Fake.nsiType().apply { outcomes = listOf(outcome) }
    val observed = subject.validateOutcome(type, request)
    assertThat(observed).isSameAs(outcome)
  }

  @Test
  fun `Asserting requirement constraints but not valid type of requirement category`() {
    val request = Fake.newNsi()
    val type = Fake.nsiType()
    val requirement = Fake.requirement()
    assertThrows<BadRequestException> { subject.assertRequirementConstraints(type, requirement, request) }
  }

  @Test
  fun `Asserting requirement constraints but terminated and no end date provided`() {
    val request = Fake.newNsi().copy(endDate = null)
    val type = Fake.nsiType()
    val requirement = Fake.requirement().apply {
      typeCategory?.nsiTypes = listOf(type)
      terminationDate = Fake.randomPastLocalDate()
    }
    assertThrows<BadRequestException> { subject.assertRequirementConstraints(type, requirement, request) }
  }

  @Test
  fun `Asserting requirement constraints but terminated and end date before termination date`() {
    val request = Fake.newNsi().copy(endDate = LocalDate.of(2021, 3, 22))
    val type = Fake.nsiType()
    val requirement = Fake.requirement().apply {
      typeCategory?.nsiTypes = listOf(type)
      terminationDate = LocalDate.of(2021, 3, 23)
    }
    assertThrows<BadRequestException> { subject.assertRequirementConstraints(type, requirement, request) }
  }

  @Test
  fun `Successfully asserting requirement constraints for terminated requirement`() {
    val request = Fake.newNsi().copy(endDate = LocalDate.of(2021, 3, 23))
    val type = Fake.nsiType()
    val requirement = Fake.requirement().apply {
      typeCategory?.nsiTypes = listOf(type)
      terminationDate = LocalDate.of(2021, 3, 23)
    }
    assertDoesNotThrow { subject.assertRequirementConstraints(type, requirement, request) }
  }

  @Test
  fun `Successfully asserting requirement constraints for non-terminated requirement`() {
    val request = Fake.newNsi()
    val type = Fake.nsiType()
    val requirement = Fake.requirement().apply {
      typeCategory?.nsiTypes = listOf(type)
      terminationDate = null
    }
    assertDoesNotThrow { subject.assertRequirementConstraints(type, requirement, request) }
  }

  @Test
  fun `Asserting supported type level but nsi at event level and event level not supported`() {
    val nsi = Fake.nsi().apply {
      type.eventLevel = false
      event = Fake.event()
    }
    assertThrows<BadRequestException> { subject.assertSupportedTypeLevel(nsi) }
  }

  @Test
  fun `Asserting supported type level but nsi at offender level and offender level not supported`() {
    val nsi = Fake.nsi().apply {
      type.offenderLevel = false
      event = null
    }
    assertThrows<BadRequestException> { subject.assertSupportedTypeLevel(nsi) }
  }

  @Test
  fun `Successfully asserting supported type level for event level nsi`() {
    val nsi = Fake.nsi().apply {
      type.eventLevel = true
      event = Fake.event()
    }
    assertDoesNotThrow { subject.assertSupportedTypeLevel(nsi) }
  }

  @Test
  fun `Successfully asserting supported type level for offender level nsi`() {
    val nsi = Fake.nsi().apply {
      type.offenderLevel = true
      event = null
    }
    assertDoesNotThrow { subject.assertSupportedTypeLevel(nsi) }
  }
}
