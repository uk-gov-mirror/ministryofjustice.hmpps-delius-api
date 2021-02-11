package uk.gov.justice.digital.hmpps.deliusapi.service.audit

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.deliusapi.entity.BusinessInteraction
import uk.gov.justice.digital.hmpps.deliusapi.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.lang.RuntimeException
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class AuditServiceTest {

  @Mock
  private lateinit var auditedInteractionRepository: AuditedInteractionRepository

  @Mock
  private lateinit var businessInteractionRepository: BusinessInteractionRepository

  @InjectMocks
  private lateinit var subject: AuditService

  @Captor
  private lateinit var entityCaptor: ArgumentCaptor<AuditedInteraction>

  private lateinit var auditedInteraction: AuditedInteraction
  private lateinit var businessInteraction: BusinessInteraction

  @BeforeEach
  fun beforeEach() {
    auditedInteraction = Fake.auditedInteraction()
    businessInteraction = Fake.businessInteraction()
  }

  @Test
  fun `Creating audited interaction using generic method`() {
    whenever(businessInteractionRepository.findByCode(AuditableInteraction.ADD_CONTACT.code)).thenReturn(businessInteraction)
    whenever(auditedInteractionRepository.saveAndFlush(any())).thenReturn(auditedInteraction)

    subject.createAuditedInteraction(
      LocalDateTime.of(2021, 1, 2, 10, 20),
      1234,
      AuditableInteraction.ADD_CONTACT,
      mapOf(AuditParameter.OFFENDER_ID to "5678"),
      true
    )

    verify(auditedInteractionRepository).saveAndFlush(
      AuditedInteraction(
        LocalDateTime.of(2021, 1, 2, 10, 20),
        "P",
        "offenderId='5678'",
        businessInteraction,
        1234
      )
    )
    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `Creating failed interaction`() {
    whenever(businessInteractionRepository.findByCode(any())).thenReturn(businessInteraction)
    whenever(auditedInteractionRepository.saveAndFlush(any())).thenReturn(auditedInteraction)

    subject.failedInteraction(1234L, 5678L, AuditableInteraction.ADD_CONTACT)
    verify(auditedInteractionRepository).saveAndFlush(entityCaptor.capture())

    assertThat(entityCaptor.value.dateTime).isNotNull
    assertThat(entityCaptor.value.outcome).isEqualTo("F")
    assertThat(entityCaptor.value.userID).isEqualTo(1234L)
    assertThat(entityCaptor.value.parameters).isEqualTo("offenderId='5678'")

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `Creating successful interaction`() {
    whenever(businessInteractionRepository.findByCode(any())).thenReturn(businessInteraction)
    whenever(auditedInteractionRepository.saveAndFlush(any())).thenReturn(auditedInteraction)

    subject.successfulInteraction(1234L, 5678L, AuditableInteraction.ADD_CONTACT)

    verify(auditedInteractionRepository).saveAndFlush(entityCaptor.capture())

    assertThat(entityCaptor.value.dateTime).isNotNull
    assertThat(entityCaptor.value.outcome).isEqualTo("P")
    assertThat(entityCaptor.value.userID).isEqualTo(1234L)
    assertThat(entityCaptor.value.parameters).isEqualTo("offenderId='5678'")

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `When no business interaction found throws exception`() {
    whenever(businessInteractionRepository.findByCode(any())).thenReturn(null)

    assertThrows<RuntimeException> {
      subject.successfulInteraction(1234L, 5678L, AuditableInteraction.ADD_CONTACT)
    }
  }

  @Test
  fun `When enabled date has not passed do not audit`() {
    val notEnabledBusinessInteraction = businessInteraction.copy(enabledDate = LocalDateTime.now().plusYears(1))
    whenever(businessInteractionRepository.findByCode(any())).thenReturn(notEnabledBusinessInteraction)

    subject.successfulInteraction(1234L, 5678L, AuditableInteraction.ADD_CONTACT)

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `When enabled date null do not audit`() {
    val notEnabledBusinessInteraction = businessInteraction.copy(enabledDate = null)
    whenever(businessInteractionRepository.findByCode(any())).thenReturn(notEnabledBusinessInteraction)

    subject.successfulInteraction(1234L, 5678L, AuditableInteraction.ADD_CONTACT)

    verifyNoMoreInteractions(auditedInteractionRepository)
  }
}
