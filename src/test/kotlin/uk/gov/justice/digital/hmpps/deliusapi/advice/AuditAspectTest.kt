package uk.gov.justice.digital.hmpps.deliusapi.advice

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditContext
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditService
import uk.gov.justice.digital.hmpps.deliusapi.service.audit.AuditableInteraction

@Component
class DummyDependency {
  fun doMoreWork() {}
}

/**
 * TODO is there a better way of unit testing aspectj?
 */
@Service
class DummyService(private val dependency: DummyDependency) {
  @Auditable(AuditableInteraction.ADD_CONTACT)
  fun doWork(setContext: Boolean = true) {
    if (setContext) {
      val audit = AuditContext.get(AuditableInteraction.ADD_CONTACT)
      audit.offenderId = 12345
    }

    dependency.doMoreWork()
  }
}

@ExtendWith(MockitoExtension::class)
class AuditAspectTest {
  @Mock
  private lateinit var auditService: AuditService

  @Mock
  private lateinit var dummyDependency: DummyDependency

  @InjectMocks
  private lateinit var auditableAspect: AuditableAspect

  @InjectMocks
  private lateinit var subject: DummyService

  @BeforeEach
  fun beforeEach() {
    val factory = AspectJProxyFactory(subject)
    factory.addAspect(auditableAspect)
    subject = factory.getProxy()
  }

  @Test
  fun `Successful interaction`() {
    subject.doWork()

    verify(auditService).createAuditedInteraction(
      eq(AuditableInteraction.ADD_CONTACT),
      eq(true),
      argThat { ctx: AuditContext -> ctx.offenderId == 12345L },
    )

    verifyNoMoreInteractions(auditService)
  }

  @Test
  fun `Failed interaction`() {
    whenever(dummyDependency.doMoreWork()).thenThrow(RuntimeException::class.java)

    assertThrows<RuntimeException> { subject.doWork() }

    verify(auditService).createAuditedInteraction(
      eq(AuditableInteraction.ADD_CONTACT),
      eq(false),
      argThat { ctx: AuditContext -> ctx.offenderId == 12345L }
    )

    verifyNoMoreInteractions(auditService)
  }

  @Test
  fun `Successful interaction & context not set`() {
    assertThrows<RuntimeException> { subject.doWork(false) }

    verifyNoMoreInteractions(auditService)
  }

  @Test
  fun `Failed interaction & context not set`() {
    whenever(dummyDependency.doMoreWork()).thenThrow(RuntimeException::class.java)

    assertThrows<RuntimeException> { subject.doWork(false) }

    verifyNoMoreInteractions(auditService)
  }
}
