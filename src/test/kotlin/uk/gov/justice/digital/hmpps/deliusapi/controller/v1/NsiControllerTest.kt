package uk.gov.justice.digital.hmpps.deliusapi.controller.v1

import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.deliusapi.service.nsi.NsiService
import uk.gov.justice.digital.hmpps.deliusapi.util.Fake

@ExtendWith(MockitoExtension::class)
class NsiControllerTest {
  @Mock
  private lateinit var service: NsiService

  @InjectMocks
  private lateinit var subject: NsiController

  @Test
  fun `Creating contact`() {
    val request = Fake.newNsi()
    val dto = Fake.nsiDto()

    whenever(service.createNsi(request)).thenReturn(dto)

    val observed = subject.create(request).body
    Assertions.assertThat(observed).isSameAs(dto)
  }
}
