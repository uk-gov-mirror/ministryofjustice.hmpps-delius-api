package uk.gov.justice.digital.hmpps.deliusapi

import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.io.File

const val SPEC_PROPERTY = "uk.gov.justice.digital.hmpps.deliusapi.specpath"

fun main(args: Array<String>) {
  val specPath = System.getProperty(SPEC_PROPERTY)
    ?: throw RuntimeException("$SPEC_PROPERTY system property is not set")

  runApplication<HmppsDeliusApi>(*args).use { context ->

    MockMvcBuilders.webAppContextSetup(context as WebApplicationContext)
      .build()
      .perform(
        MockMvcRequestBuilders.get("/v3/api-docs")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andDo { File(specPath).writeText(it.response.contentAsString) }

    context.registerShutdownHook()
    context.stop()
  }
}
