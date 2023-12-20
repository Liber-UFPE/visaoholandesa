package br.ufpe.liber.controllers

import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.http.server.types.files.StreamedFile
import io.micronaut.http.server.util.HttpHostResolver

@Controller
class SeoController(private val resourceResolver: ResourceResolver, private val hostResolver: HttpHostResolver) {
    @Get("/sitemap.xml")
    @Produces(MediaType.TEXT_XML)
    fun sitemap(): HttpResponse<StreamedFile> = resourceResolver
        .getResourceAsStream("classpath:public/sitemap.xml")
        .map { HttpResponse.ok(StreamedFile(it, MediaType.TEXT_XML_TYPE)) }
        .orElse(HttpResponse.notFound())

    @Get("/robots.txt")
    @Produces(MediaType.TEXT_PLAIN)
    fun robots(): String {
        val host = currentRequest().map(hostResolver::resolve).orElse("")
        return """
        User-agent: *
        Allow: /

        Sitemap: $host/sitemap.xml
        """.trimIndent().trim()
    }

    private fun currentRequest() = ServerRequestContext.currentRequest<Any>()
}
