package uk.gov.justice.digital.hmpps.deliusapi.config

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev")
class DevH2Configuration {

  @Bean(initMethod = "start", destroyMethod = "stop")
  fun inMemoryH2DatabaseaServer(): Server = Server.createTcpServer(
    "-tcp",
    "-tcpAllowOthers",
    "-tcpPort",
    "9092"
  )
}
