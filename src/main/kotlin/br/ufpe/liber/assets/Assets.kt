package br.ufpe.liber.assets

import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.MediaType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Asset(
    val original: String,
    val hash: String,
    val integrity: String,
    val extension: String,
    val mediaType: String,
) {
    fun mediaType(): MediaType = MediaType(mediaType)

    fun fullpath(prefix: String = ""): String = if (prefix.isBlank()) {
        "${original.removePrefix("/")}.$hash.$extension"
    } else {
        "$prefix/${original.removePrefix("/")}.$hash.$extension"
    }

    fun variant(newExtension: String, prefix: String = ""): String = if (prefix.isBlank()) {
        "${original.removePrefix("/")}.$hash.$newExtension"
    } else {
        "$prefix/${original.removePrefix("/")}.$hash.$newExtension"
    }

    fun classpath(encoding: String = ""): String {
        val resourcePath = fullpath()
        val resourcePathWithEncoding = if (encoding.isBlank()) resourcePath else "$resourcePath.$encoding"
        return "classpath:public/$resourcePathWithEncoding"
    }
}
