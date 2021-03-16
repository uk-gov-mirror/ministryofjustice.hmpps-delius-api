package uk.gov.justice.digital.hmpps.deliusapi.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Table

@Embeddable
data class ProviderUserIdentity(
  @Column(name = "USER_ID")
  var userId: Long,

  @Column(name = "PROBATION_AREA_ID")
  var providerId: Long,
) : Serializable

@Entity
@Table(name = "PROBATION_AREA_USER")
class ProviderUser(
  @EmbeddedId
  var id: ProviderUserIdentity,

  @MapsId("providerId")
  @JoinColumn(name = "PROBATION_AREA_ID")
  @ManyToOne
  var provider: Provider? = null,
)
