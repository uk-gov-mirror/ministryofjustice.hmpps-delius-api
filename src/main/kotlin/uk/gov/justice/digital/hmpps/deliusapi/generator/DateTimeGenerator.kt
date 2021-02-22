package uk.gov.justice.digital.hmpps.deliusapi.generator

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.io.Serializable
import java.time.LocalDateTime

class DateTimeGenerator : IdentifierGenerator {
  override fun generate(session: SharedSessionContractImplementor?, `object`: Any?): Serializable = LocalDateTime.now()
}
