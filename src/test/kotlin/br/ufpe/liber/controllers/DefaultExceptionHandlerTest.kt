package br.ufpe.liber.controllers

import br.ufpe.liber.StaticTemplates
import br.ufpe.liber.shouldContain
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.mockk.every
import io.mockk.mockk
import java.net.URI

class DefaultExceptionHandlerTest : BehaviorSpec({

    val exceptionHandler = DefaultExceptionHandler(StaticTemplates())

    given("Default ExceptionHandler") {
        `when`("there is a server error") {
            then("it should return 500 Internal Server Error") {
                val request: HttpRequest<Any> = mockk()
                val exception = Exception("The error message")

                every { request.path } answers { "/testing-path" }
                every { request.method } answers { HttpMethod.GET }
                every { request.uri } answers { URI.create("http://localhost:8080/") }

                val response = exceptionHandler.handle(request, exception)
                response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR)
                response.body().shouldContain(exception.message.toString())
                response.header(HttpHeaders.CONTENT_TYPE) shouldBe "text/html; charset=utf-8"
            }
        }
    }
})
