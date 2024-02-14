package br.ufpe.liber.views

import io.micronaut.http.context.ServerRequestContext
import io.micronaut.views.csp.CspFilter.NONCE_PROPERTY

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.
object CSPHelper {
    fun nonce(): String = ServerRequestContext.currentRequest<Any>()
        .flatMap { request -> request.getAttribute(NONCE_PROPERTY).map { attr -> attr.toString() } }
        .orElse("")
}
