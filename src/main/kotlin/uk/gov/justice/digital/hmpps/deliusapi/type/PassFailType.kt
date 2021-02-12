package uk.gov.justice.digital.hmpps.deliusapi.type

import org.hibernate.dialect.Dialect
import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.type.DiscriminatorType
import org.hibernate.type.PrimitiveType
import org.hibernate.type.StringType
import org.hibernate.type.descriptor.java.BooleanTypeDescriptor
import org.hibernate.type.descriptor.sql.CharTypeDescriptor
import java.io.Serializable
import java.lang.Exception

class PassFailType : AbstractSingleColumnStandardBasicType<Boolean?>(CharTypeDescriptor.INSTANCE, BooleanTypeDescriptor.INSTANCE), PrimitiveType<Boolean?>, DiscriminatorType<Boolean?> {

  override fun getName(): String {
    return "pass_fail"
  }

  override fun getPrimitiveClass(): Class<*>? {
    return Boolean::class.javaPrimitiveType
  }

  @Throws(Exception::class)
  override fun stringToObject(xml: String): Boolean {
    return fromString(xml)!!
  }

  override fun getDefaultValue(): Serializable {
    return java.lang.Boolean.FALSE
  }

  @Throws(Exception::class)
  override fun objectToSQLString(value: Boolean?, dialect: Dialect?): String? {
    return StringType.INSTANCE.objectToSQLString(if (value == true) "P" else "F", dialect)
  }

  companion object {
    val INSTANCE = PassFailType()
  }
}
