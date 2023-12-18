package br.ufpe.liber.assets

import br.ufpe.liber.EagerInProduction
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.MediaType
import jakarta.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.util.NavigableMap
import java.util.Optional
import java.util.TreeMap

@Singleton
@EagerInProduction
class AssetsResolver(resourceResolver: ResourceResolver) {
    private val assets: NavigableMap<String, Asset> = TreeMap()
    private val assetsWithHashedVersionAsKeys: NavigableMap<String, Asset> = TreeMap()

    companion object {
        lateinit var instance: AssetsResolver
    }

    init {
        resourceResolver.getResourceAsStream("classpath:public/assets-metadata.json").ifPresent { inputStream ->
            val json = inputStream.bufferedReader().use(BufferedReader::readText)
            val fromJson: Map<String, Asset> = Json.decodeFromString(json)
            assets.putAll(fromJson)

            fromJson.forEach { (_, asset) ->
                assetsWithHashedVersionAsKeys[asset.filename] = asset
            }
        }

        instance = this
    }

    fun at(path: String): Optional<Asset> = Optional.ofNullable(assets[path])
    fun fromHashed(path: String): Optional<Asset> = Optional.ofNullable(assetsWithHashedVersionAsKeys[path])
}

object AssetsViewHelpers {
    fun at(path: String) = AssetsResolver.instance.at(path)
}

@Serializable
data class Asset(
    val basename: String,
    val source: String,
    val filename: String,
    val hash: String,
    val integrity: String,
    val extension: String,
    val mediaType: String,
    val encodings: List<Encoding> = emptyList(),
) {
    fun mediaType(): MediaType = MediaType(mediaType)

    @Transient
    private val unprefixedFilename: String = filename.removePrefix("/")

    @Transient
    private val sortedEncodings: List<Encoding> = encodings.sorted()

    fun fullpath(prefix: String = ""): String = if (prefix.isBlank()) {
        unprefixedFilename
    } else {
        "$prefix/$unprefixedFilename"
    }

    fun variant(newExtension: String, prefix: String = ""): String = if (prefix.isBlank()) {
        "${basename.removePrefix("/")}.$hash.$newExtension"
    } else {
        "$prefix/${basename.removePrefix("/")}.$hash.$newExtension"
    }

    fun classpath(encoding: String = ""): String {
        val resourcePath = fullpath()
        val resourcePathWithEncoding = if (encoding.isBlank()) resourcePath else "$resourcePath.$encoding"
        return "classpath:public/$resourcePathWithEncoding"
    }

    fun preferredEncodedResource(acceptEncoding: String): Optional<Encoding> = Optional.ofNullable(
        sortedEncodings.firstOrNull { acceptEncoding.contains(it.http) },
    )
}

@Serializable
data class Encoding(val http: String, val extension: String, val priority: Int) : Comparable<Encoding> {
    override fun compareTo(other: Encoding): Int = this.priority.compareTo(other.priority)
}
