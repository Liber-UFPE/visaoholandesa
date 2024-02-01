package br.ufpe.liber.tasks

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun String?.safeToJson(): JsonPrimitive = if (this == null) {
    JsonNull
} else {
    JsonPrimitive(this)
}
fun String.toJson(): JsonPrimitive = JsonPrimitive(this)
fun Number.toJson(): JsonPrimitive = JsonPrimitive(this)
fun Map<String, JsonElement>.toJson() = JsonObject(this)
