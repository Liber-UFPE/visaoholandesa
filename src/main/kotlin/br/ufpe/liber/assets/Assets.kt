package br.ufpe.liber.assets

import br.ufpe.liber.EagerInProduction
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.MediaType
import jakarta.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.util.NavigableMap
import java.util.Optional
import java.util.TreeMap

@Singleton
@EagerInProduction
class AssetsResolver(resourceResolver: ResourceResolver) {
    private val assets: NavigableMap<String, Asset> = TreeMap()

    companion object {
        lateinit var instance: AssetsResolver
    }

    init {
        resourceResolver.getResourceAsStream("classpath:public/assets-metadata.json").ifPresent { inputStream ->
            val json = inputStream.bufferedReader().use(BufferedReader::readText)
            val fromJson: Map<String, Asset> = Json.decodeFromString(json)
            assets.putAll(fromJson)
        }

        instance = this
    }

    fun at(path: String): Optional<Asset> = Optional.ofNullable(assets[path])

    fun fromHashed(path: String): Optional<Asset> {
        val pathParts = path.split(".")
        val extension = pathParts.last()
        val basePath = pathParts.dropLast(2).joinToString(".")

        return at("$basePath.$extension")
    }
}

object AssetsViewHelpers {
    fun at(path: String) = AssetsResolver.instance.at(path)
}

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
