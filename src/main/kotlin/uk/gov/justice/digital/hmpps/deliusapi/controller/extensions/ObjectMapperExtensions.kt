package uk.gov.justice.digital.hmpps.deliusapi.controller.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import uk.gov.justice.digital.hmpps.deliusapi.exception.BadJsonPatchException

inline fun <reified T> ObjectMapper.applyPatch(name: String, patch: JsonPatch, request: T): T = try {
  val node = convertValue(request, JsonNode::class.java)
  val patchedNode = patch.apply(node)
  treeToValue(patchedNode, T::class.java)
} catch (e: Exception) {
  throw BadJsonPatchException(name, e)
}
