package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Produces

@Controller("/errors")
@Produces(MediaType.TEXT_HTML)
class ErrorsController(private val templates: Templates) : KteController {
    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun defaultNotFound(request: HttpRequest<Any>): HttpResponse<KteWriteable> =
        notFound(templates.notFound(request.path))
}
