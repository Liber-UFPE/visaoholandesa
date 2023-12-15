package br.ufpe.liber.controllers

import br.ufpe.liber.assets.Asset
import br.ufpe.liber.assets.AssetsResolver
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.server.types.files.StreamedFile
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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
        @Suppress("detekt:MagicNumber")
        val ONE_YEAR_IN_SECONDS: Long = TimeUnit.DAYS.toSeconds(365)
        val HTTP_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            .withZone(ZoneId.of("GMT"))
    }

    object Encoding {
        const val BROTLI = "br"
        const val GZIP = "gzip"
        const val DEFLATE = "deflate"
    }

    object Extensions {
        const val BROTLI = "br"
        const val GZIP = "gz"
        const val DEFLATE = "zz"
    }

    @Get
    fun asset(@Header("Accept-Encoding") encoding: String, path: String): HttpResponse<StreamedFile> {
        return assetsResolver
            .fromHashed("/$path")
            .flatMap { asset ->
                if (encoding.contains(Encoding.BROTLI)) {
                    serveBrotli(asset)
                } else if (encoding.contains(Encoding.GZIP)) {
                    serveGzip(asset)
                } else if (encoding.contains(Encoding.DEFLATE)) {
                    serveDeflate(asset)
                } else {
                    servePlain(asset)
                }
            }.orElse(HttpResponse.notFound())
    }

    private fun serveBrotli(asset: Asset): Optional<HttpResponse<StreamedFile>> {
        return serveAsset(asset, Extensions.BROTLI, Encoding.BROTLI)
    }

    private fun serveGzip(asset: Asset): Optional<HttpResponse<StreamedFile>> {
        return serveAsset(asset, Extensions.GZIP, Encoding.GZIP)
    }

    private fun serveDeflate(asset: Asset): Optional<HttpResponse<StreamedFile>> {
        return serveAsset(asset, Extensions.DEFLATE, Encoding.DEFLATE)
    }

    private fun servePlain(asset: Asset): Optional<HttpResponse<StreamedFile>> {
        return resourceResolver.getResourceAsStream(asset.classpath()).map { inputStream ->
            HttpResponse.ok(StreamedFile(inputStream, asset.mediaType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=${Cache.ONE_YEAR_IN_SECONDS}, immutable")
                .header(HttpHeaders.EXPIRES, oneYearFromNow())
        }
    }

    private fun serveAsset(asset: Asset, extension: String, encoding: String): Optional<HttpResponse<StreamedFile>> {
        return resourceResolver
            .getResourceAsStream(asset.classpath(extension))
            .map { inputStream ->
                HttpResponse.ok(StreamedFile(inputStream, asset.mediaType())).contentEncoding(encoding)
            }
            .or {
                // Not all assets will have brotli/gzip/deflate versions. For example, it does not make
                // sense to compress webp images.
                resourceResolver
                    .getResourceAsStream(asset.classpath())
                    .map { inputStream -> HttpResponse.ok(StreamedFile(inputStream, asset.mediaType())) }
            }
            .map {
                it.header(HttpHeaders.CACHE_CONTROL, "public, max-age=${Cache.ONE_YEAR_IN_SECONDS}, immutable")
                    .header(HttpHeaders.EXPIRES, oneYearFromNow())
            }
    }

    private fun oneYearFromNow(): String {
        return LocalDateTime.now(ZoneOffset.UTC).plusYears(1).format(Cache.HTTP_DATE_FORMATTER)
    }
}
