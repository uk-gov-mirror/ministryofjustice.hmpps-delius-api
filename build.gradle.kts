plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.0.0"
  kotlin("plugin.spring") version "1.4.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("io.springfox:springfox-boot-starter:3.0.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.4")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("com.github.javafaker:javafaker:1.0.2")
}
