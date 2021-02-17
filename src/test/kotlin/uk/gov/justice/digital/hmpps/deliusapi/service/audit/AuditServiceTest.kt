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
import uk.gov.justice.digital.hmpps.deliusapi.service.SecurityUserContext
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake
import java.lang.RuntimeException
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class AuditServiceTest {

  @Mock
  private lateinit var auditedInteractionRepository: AuditedInteractionRepository

  @Mock
  private lateinit var businessInteractionRepository: BusinessInteractionRepository

  @Mock
  private lateinit var securityUserContext: SecurityUserContext

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
    whenever(businessInteractionRepository.findFirstByCode(AuditableInteraction.ADD_CONTACT.code)).thenReturn(businessInteraction)
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
        true,
        "offenderId='5678'",
        businessInteraction,
        1234
      )
    )
    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `Creating failed interaction`() {
    whenever(businessInteractionRepository.findFirstByCode(any())).thenReturn(businessInteraction)
    whenever(auditedInteractionRepository.saveAndFlush(any())).thenReturn(auditedInteraction)
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(1234)

    subject.failedInteraction(AuditableInteraction.ADD_CONTACT, AuditContext(5678))
    verify(auditedInteractionRepository).saveAndFlush(entityCaptor.capture())

    assertThat(entityCaptor.value.dateTime).isNotNull
    assertThat(entityCaptor.value.success).isFalse
    assertThat(entityCaptor.value.userId).isEqualTo(1234)
    assertThat(entityCaptor.value.parameters).isEqualTo("offenderId='5678'")

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `Creating successful interaction`() {
    whenever(businessInteractionRepository.findFirstByCode(any())).thenReturn(businessInteraction)
    whenever(auditedInteractionRepository.saveAndFlush(any())).thenReturn(auditedInteraction)
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(1234)

    subject.successfulInteraction(AuditableInteraction.ADD_CONTACT, AuditContext(5678))

    verify(auditedInteractionRepository).saveAndFlush(entityCaptor.capture())

    assertThat(entityCaptor.value.dateTime).isNotNull
    assertThat(entityCaptor.value.success).isTrue
    assertThat(entityCaptor.value.userId).isEqualTo(1234)
    assertThat(entityCaptor.value.parameters).isEqualTo("offenderId='5678'")

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `Creating successful NSI interaction`() {
    whenever(businessInteractionRepository.findFirstByCode(any())).thenReturn(businessInteraction)
    whenever(auditedInteractionRepository.saveAndFlush(any())).thenReturn(auditedInteraction)
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(1234)

    subject.successfulInteraction(AuditableInteraction.ADD_CONTACT, AuditContext(nsiId = 3434))

    verify(auditedInteractionRepository).saveAndFlush(entityCaptor.capture())

    assertThat(entityCaptor.value.dateTime).isNotNull
    assertThat(entityCaptor.value.success).isTrue
    assertThat(entityCaptor.value.userId).isEqualTo(1234)
    assertThat(entityCaptor.value.parameters).isEqualTo("nsiId='3434'")

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `Creating interaction with no parameters fails`() {
    assertThrows<RuntimeException> {
      subject.successfulInteraction(AuditableInteraction.ADD_CONTACT, AuditContext())
    }
  }

  @Test
  fun `When no business interaction found throws exception`() {
    whenever(businessInteractionRepository.findFirstByCode(any())).thenReturn(null)
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(1234)

    assertThrows<RuntimeException> {
      subject.successfulInteraction(AuditableInteraction.ADD_CONTACT, AuditContext(5678))
    }
  }

  @Test
  fun `When enabled date has not passed do not audit`() {
    val notEnabledBusinessInteraction = businessInteraction.copy(enabledDate = LocalDateTime.now().plusYears(1))
    whenever(businessInteractionRepository.findFirstByCode(any())).thenReturn(notEnabledBusinessInteraction)
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(1234)

    subject.successfulInteraction(AuditableInteraction.ADD_CONTACT, AuditContext(5678))

    verifyNoMoreInteractions(auditedInteractionRepository)
  }

  @Test
  fun `When enabled date null do not audit`() {
    val notEnabledBusinessInteraction = businessInteraction.copy(enabledDate = null)
    whenever(businessInteractionRepository.findFirstByCode(any())).thenReturn(notEnabledBusinessInteraction)
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(1234)

    subject.successfulInteraction(AuditableInteraction.ADD_CONTACT, AuditContext(5678))

    verifyNoMoreInteractions(auditedInteractionRepository)
  }
}
