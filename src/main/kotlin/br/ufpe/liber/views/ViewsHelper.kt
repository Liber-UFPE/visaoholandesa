package br.ufpe.liber.views

import gg.jte.html.HtmlContent
import gg.jte.html.HtmlTemplateOutput
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.views.htmx.http.HtmxRequestUtils

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

object ViewsHelper {
    fun isHtmxRequest(): Boolean = ServerRequestContext
        .currentRequest<Any>()
        .map { HtmxRequestUtils.isHtmxRequest(it) }
        .orElse(false)

    fun notHtmxRequest(): Boolean = !isHtmxRequest()

    fun isActive(path: String): Boolean = ServerRequestContext.currentRequest<Any>()
        .map { req -> req.path == path }
        .orElse(false)

    fun emptyContent(): HtmlContent = object : HtmlContent {
        override fun writeTo(output: HtmlTemplateOutput) {
            // nothing to do, empty content
        }

        override fun isEmptyContent(): Boolean = true
    }
}
