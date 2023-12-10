package br.ufpe.liber.views

import gg.jte.html.HtmlContent
import gg.jte.html.HtmlTemplateOutput
import io.micronaut.http.context.ServerRequestContext
import java.util.Optional

object ViewsHelper {

    const val HX_TARGET_HEADER: String = "HX-Target"
    const val HX_REQUEST_HEADER: String = "HX-Request"

    fun htmxTarget(): Optional<Pair<String, String>> = ServerRequestContext
        .currentRequest<Any>()
        .flatMap { Optional.ofNullable(it.headers[HX_TARGET_HEADER]) }
        .map { Pair(HX_TARGET_HEADER, it) }

    fun htmxRequest(): Optional<Pair<String, Boolean>> = ServerRequestContext
        .currentRequest<Any>()
        .flatMap { Optional.ofNullable(it.headers[HX_REQUEST_HEADER]) }
        .map { Pair(HX_REQUEST_HEADER, it.toBoolean()) }

    fun isHtmxRequest(): Boolean = htmxRequest().map { it.second }.orElse(false)
    fun notHtmxRequest(): Boolean = !isHtmxRequest()

    fun emptyContent(): HtmlContent = object : HtmlContent {
        override fun writeTo(output: HtmlTemplateOutput) {
            // nothing to do, empty content
        }

        override fun isEmptyContent(): Boolean = true
    }
}
