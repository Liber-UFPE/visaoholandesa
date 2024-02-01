package br.ufpe.liber.controllers

import br.ufpe.liber.assets.Asset
import br.ufpe.liber.assets.AssetsResolver
import br.ufpe.liber.assets.CacheControl
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.server.types.files.StreamedFile
import java.time.Duration
import java.time.LocalDateTime
import java.util.Optional

@Controller("/static/{+path}")
class AssetsController(
    private val assetsResolver: AssetsResolver,
    private val resourceResolver: ResourceResolver,
) {
    @Get
    fun asset(
        path: String,
        @Header("Accept-Encoding") encoding: String,
        @Header("If-None-Match") ifNoneMatch: Optional<String> = Optional.empty(),
    ): HttpResponse<StreamedFile> {
        val maybeAsset = assetsResolver.fromHashed("/$path")
        if (maybeAsset.isEmpty) return HttpResponse.notFound()

        val asset = maybeAsset.get()
        return notModified(asset, ifNoneMatch).orElseGet { httpResponseForAsset(asset, encoding) }
    }

    private fun notModified(asset: Asset, ifNoneMatch: Optional<String>): Optional<HttpResponse<StreamedFile>> {
        // Handle Etags:
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-None-Match
        return ifNoneMatch
            .filter { etags ->
                // Syntax for `If-None-Match` header:
                // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-None-Match#syntax
                etags.split(",").any { etag -> etag.trim() == asset.etag }
            }
            .map { HttpResponse.notModified() }
    }

    private fun httpResponseForAsset(asset: Asset, encoding: String): HttpResponse<StreamedFile> {
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
            .or { tryPlainAsset(asset) }
            .map { response -> setCacheHeaders(asset, response) }
            .orElse(HttpResponse.notFound())
    }

    private fun tryPlainAsset(asset: Asset): Optional<MutableHttpResponse<StreamedFile>> = resourceResolver
        .getResourceAsStream(asset.classpath())
        .map { inputStream -> HttpResponse.ok(StreamedFile(inputStream, asset.mediaType())) }

    @Suppress("MagicNumber", "MAGIC_NUMBER")
    private fun setCacheHeaders(
        asset: Asset,
        response: MutableHttpResponse<StreamedFile>,
    ): MutableHttpResponse<StreamedFile> {
        val maxAge = Duration.ofDays(365) // one year
        response.headers
            .lastModified(asset.lastModified)
            .expires(LocalDateTime.now().plus(maxAge))
            .add(
                HttpHeaders.CACHE_CONTROL,
                CacheControl(maxAge = maxAge, immutable = true, public = true).toString(),
            )
            .add(HttpHeaders.ETAG, asset.etag)
        return response
    }
}
