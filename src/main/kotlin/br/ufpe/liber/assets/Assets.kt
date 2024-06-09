package br.ufpe.liber.assets

import br.ufpe.liber.EagerInProduction
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.MediaType
import jakarta.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import java.lang.IllegalArgumentException
import java.util.NavigableMap
import java.util.Optional
import java.util.TreeMap
import kotlin.jvm.optionals.getOrDefault

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.
@OptIn(ExperimentalSerializationApi::class)
@Singleton
@EagerInProduction
class AssetsResolver(resourceResolver: ResourceResolver) {
    private val assets: NavigableMap<String, Asset> = TreeMap()
    private val assetsWithHashedVersionAsKeys: NavigableMap<String, Asset> = TreeMap()

    companion object {
        lateinit var instance: AssetsResolver
    }

    init {
        resourceResolver.getResource("classpath:public/assets-metadata.json").ifPresent { url ->
            Json.decodeToSequence<Asset>(url.openStream()).forEach { asset: Asset ->
                assets[asset.source] = asset
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
    fun fingerprinted(prefix: String, path: String): String = at(path)
        .map { it.fullpath(prefix) }
        .orElseThrow { IllegalArgumentException("Could not find asset for path $path") }
}

@Serializable
data class Asset(
    val basename: String,
    val source: String,
    val filename: String,
    val hash: String,
    val integrity: String,
    val etag: String,
    val lastModified: Long,
    val extension: String,
    val mediaType: String,
    val supportedEncodings: List<Encoding> = emptyList(),
) {
    fun mediaType(): MediaType = MediaType(mediaType)

    @Transient
    private val unprefixedFilename: String = filename.removePrefix("/")

    @Transient
    private val sortedEncodings: List<Encoding> = supportedEncodings.sorted()

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

    fun preferredEncodedResource(acceptEncoding: String): Optional<Encoding> {
        val acceptEncodings = AcceptEncoding.parseHeader(acceptEncoding).sorted()

        // Will handle the resource without a Content-Encoding
        if (acceptEncodings.isEmpty()) return Optional.empty()

        // If none have a quality value, use whatever is "better"
        // for the server AND supported by the client.
        return if (acceptEncodings.none { ac -> ac.q.isPresent }) {
            Optional.ofNullable(sortedEncodings.firstOrNull { acceptEncoding.contains(it.http) })
        } else {
            // Return based on supported encodings and quality values
            // for requested accept-encodings.
            Optional.ofNullable(
                supportedEncodings.maxByOrNull { supportedEnc ->
                    acceptEncodings.firstOrNull { acceptEnc ->
                        acceptEnc.name == supportedEnc.http
                    }?.qualityValue ?: 1f
                },
            )
        }
    }
}

@Serializable
data class Encoding(val http: String, val extension: String, val priority: Int) : Comparable<Encoding> {
    override fun compareTo(other: Encoding): Int = this.priority.compareTo(other.priority)
}

@Suppress("IDENTIFIER_LENGTH") // it is okay to use `q` because it is part of the spec.
data class AcceptEncoding(
    val name: String,
    val q: Optional<Float> = Optional.empty(),
) : Comparable<AcceptEncoding> {
    @Suppress("MAGIC_NUMBER")
    val qualityValue = q.getOrDefault(1f)

    override fun compareTo(other: AcceptEncoding): Int = other.qualityValue.compareTo(this.qualityValue)

    companion object {
        private val QUALITY_VALUE_FORMAT = "^[0-1](\\.[0-9]{1,3})?$".toRegex()
        fun parseHeader(header: String): List<AcceptEncoding> = header.split(",")
            .map { entry -> parseEntry(entry.trim()) }
            .filter { it.qualityValue != 0f }

        private fun parseEntry(entry: String): AcceptEncoding {
            val parts = entry.split(";")
            val name = parts.first()
            return if (parts.size > 1) {
                AcceptEncoding(name, parseQualityValue(parts[1]))
            } else {
                AcceptEncoding(name)
            }
        }

        private fun parseQualityValue(q: String): Optional<Float> {
            val parts = q.split("=")

            if (parts.size <= 1) return Optional.empty()

            return if (notAcceptableQualityValue(parts[1].trim())) {
                Optional.empty()
            } else {
                Optional.of(parts[1].trim().toFloat())
            }
        }

        // Not precise, but good enough to adhere to https://datatracker.ietf.org/doc/html/rfc9110#section-12.4.2
        private fun notAcceptableQualityValue(value: String): Boolean = !value.matches(QUALITY_VALUE_FORMAT)
    }
}
