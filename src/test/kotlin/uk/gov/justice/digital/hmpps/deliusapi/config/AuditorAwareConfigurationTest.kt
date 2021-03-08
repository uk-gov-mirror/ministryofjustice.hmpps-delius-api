package uk.gov.justice.digital.hmpps.deliusapi.config

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.service.security.SecurityUserContext

@ExtendWith(MockitoExtension::class)
internal class AuditorAwareConfigurationTest {

  @Mock
  private lateinit var securityUserContext: SecurityUserContext

  @InjectMocks
  private lateinit var auditorAwareConfiguration: AuditorAwareConfiguration

  @Test
  fun `getCurrentAuditor gets userid from security context`() {
    val userId = 12345678L
    whenever(securityUserContext.getCurrentDeliusUserId()).thenReturn(userId)
    assertThat(auditorAwareConfiguration.currentAuditor.get()).isEqualTo(userId)
    verify(securityUserContext).getCurrentDeliusUserId()
  }
}
