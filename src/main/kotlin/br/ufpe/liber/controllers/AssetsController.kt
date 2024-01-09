package br.ufpe.liber.controllers

import br.ufpe.liber.assets.Asset
import br.ufpe.liber.assets.AssetsResolver
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.server.types.files.StreamedFile
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Optional
import java.util.concurrent.TimeUnit

@Controller("/static/{+path}")
class AssetsController(
    private val assetsResolver: AssetsResolver,
    private val resourceResolver: ResourceResolver,
) {
    object Cache {
        @Suppress("detekt:MagicNumber", "MAGIC_NUMBER")
        val ONE_YEAR_IN_SECONDS: Long = TimeUnit.DAYS.toSeconds(365)
        val HTTP_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            .withZone(ZoneId.of("GMT"))
    }

    @Get
    fun asset(@Header("Accept-Encoding") encoding: String, path: String): HttpResponse<StreamedFile> {
        return assetsResolver
            .fromHashed("/$path")
            .flatMap { asset -> httpResponseForAsset(asset, encoding) }
            .map { response ->
                response
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=${Cache.ONE_YEAR_IN_SECONDS}, immutable")
                    .header(HttpHeaders.EXPIRES, oneYearFromNow())
            }
            .orElse(HttpResponse.notFound())
    }

    private fun httpResponseForAsset(asset: Asset, encoding: String): Optional<MutableHttpResponse<StreamedFile>> {
        return asset
            .preferredEncodedResource(encoding)
            .flatMap { availableEncoding ->
                resourceResolver
                    .getResourceAsStream(asset.classpath(availableEncoding.extension))
                    .map { inputStream ->
                        HttpResponse
                            .ok(StreamedFile(inputStream, asset.mediaType()))
                            .contentEncoding(availableEncoding.http)
                    }
            }
            .or {
                resourceResolver
                    .getResourceAsStream(asset.classpath())
                    .map { inputStream -> HttpResponse.ok(StreamedFile(inputStream, asset.mediaType())) }
            }
    }

    private fun oneYearFromNow(): String = LocalDateTime.now(UTC).plusYears(1).format(Cache.HTTP_DATE_FORMATTER)
}
