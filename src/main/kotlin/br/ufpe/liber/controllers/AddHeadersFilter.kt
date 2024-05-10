package br.ufpe.liber.controllers

import io.micronaut.core.order.Ordered
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.ResponseFilter
import io.micronaut.http.annotation.ServerFilter
import io.micronaut.http.filter.ServerFilterPhase

@ServerFilter(Filter.MATCH_ALL_PATTERN)
@Suppress("CLASS_NAME_INCORRECT")
class AddHeadersFilter : Ordered {
    @ResponseFilter
    fun addHeader(res: MutableHttpResponse<Any>) = res.headers(
        mapOf(
            "X-Content-Type-Options" to "nosniff",
            "Referrer-Policy" to "no-referrer, strict-origin-when-cross-origin",
        ),
    )

    override fun getOrder(): Int = ServerFilterPhase.LAST.order()
}
