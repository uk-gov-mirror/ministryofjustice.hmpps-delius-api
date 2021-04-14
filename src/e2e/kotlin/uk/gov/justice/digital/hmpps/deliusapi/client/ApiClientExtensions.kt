package uk.gov.justice.digital.hmpps.deliusapi.client

import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.ClientError
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.infrastructure.Serializer
import org.openapitools.client.infrastructure.ServerError
import uk.gov.justice.digital.hmpps.deliusapi.config.ErrorResponse

/**
 * Default api client doesn't add the response body to thrown exception messages.
 * Our exceptions need this as we return error reasons in the body rather than the status text.
 */
fun <Api : ApiClient, T> Api.safely(act: (api: Api) -> T): T {
  try {
    return act(this)
  } catch (e: ClientException) {
    val bodyContent = when (val response = e.response) {
      is ClientError<*> -> response.body?.toString()
      is ServerError<*> -> response.body?.toString()
      else -> null
    }
    val error = if (bodyContent == null || bodyContent.isBlank()) null
    else Serializer.moshi.adapter(ErrorResponse::class.java).fromJson(bodyContent)

    throw ApiException(e.statusCode, error, e)
  }
}

class ApiException(val statusCode: Int, val error: ErrorResponse?, cause: Exception? = null) :
  RuntimeException("request failed with code $statusCode ${error ?: "[but no body]"}", cause)
