package br.ufpe.liber.controllers

import gg.jte.models.runtime.JteModel
import gg.jte.output.WriterOutput
import io.micronaut.core.io.Writable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import java.io.Writer

interface KteController {
    companion object {
        // Micronaut does not set the charset for responses. So it is now done
        // manually here. For more details, see:
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type
        private const val DEFAULT_CONTENT_TYPE = "${MediaType.TEXT_HTML}; charset=utf-8"
    }

    fun ok(model: JteModel): HttpResponse<KteWriteable> =
        HttpResponse.ok(KteWriteable(model)).contentType(DEFAULT_CONTENT_TYPE)

    fun notFound(model: JteModel): HttpResponse<KteWriteable> =
        HttpResponse.notFound(KteWriteable(model)).contentType(DEFAULT_CONTENT_TYPE)

    fun serverError(model: JteModel): HttpResponse<KteWriteable> =
        HttpResponse.serverError(KteWriteable(model)).contentType(DEFAULT_CONTENT_TYPE)
}

class KteWriteable(private val model: JteModel) : Writable {
    override fun writeTo(out: Writer?) = model.render(WriterOutput(out))
}
