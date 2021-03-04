package uk.gov.justice.digital.hmpps.deliusapi.type

import java.sql.Time
import java.time.LocalTime
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class LocalTimeAttributeConverter : AttributeConverter<LocalTime?, Time?> {
  override fun convertToDatabaseColumn(localTime: LocalTime?): Time? =
    if (localTime == null) null else Time.valueOf(localTime)

  override fun convertToEntityAttribute(time: Time?): LocalTime? = time?.toLocalTime()
}
