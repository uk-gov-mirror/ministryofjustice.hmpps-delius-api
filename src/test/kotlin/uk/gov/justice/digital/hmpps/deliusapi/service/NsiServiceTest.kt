package uk.gov.justice.digital.hmpps.deliusapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.dto.v1.NsiDto
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
class NsiServiceTest {
  @InjectMocks
  private lateinit var subject: NsiService

  @Test
  fun `Creating nsi`() {
    val request = Fake.newNsi()
    val observed = subject.createNsi(request)
    assertThat(observed).isInstanceOf(NsiDto::class.java)
  }
}
