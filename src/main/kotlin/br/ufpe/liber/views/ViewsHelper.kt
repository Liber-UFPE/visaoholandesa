package br.ufpe.liber.views

import gg.jte.Content
import gg.jte.html.HtmlTemplateOutput
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.views.turbo.http.TurboHttpHeaders.TURBO_FRAME
import java.util.Optional

object ViewsHelper {
    fun turboFrame(): Optional<String> = ServerRequestContext.currentRequest<Any>().map { it.headers[TURBO_FRAME] }
    fun isTurboRequest(): Boolean = turboFrame().isPresent
    fun notTurboRequest(): Boolean = turboFrame().isEmpty

    fun emptyContent(): Content = object : gg.jte.html.HtmlContent {
        override fun writeTo(output: HtmlTemplateOutput) {
            // nothing to do, empty content
        }

        override fun isEmptyContent(): Boolean = true
    }
}
