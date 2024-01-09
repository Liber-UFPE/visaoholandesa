package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
@Produces(MediaType.TEXT_HTML)
@Requires(classes = [Exception::class, ExceptionHandler::class])
class DefaultExceptionHandler(private val templates: Templates) :
    ExceptionHandler<Exception, HttpResponse<*>>, KteController {
    override fun handle(request: HttpRequest<*>, exception: Exception): HttpResponse<KteWriteable> =
        serverError(templates.internalServerError(request.path, exception.message!!))
}
