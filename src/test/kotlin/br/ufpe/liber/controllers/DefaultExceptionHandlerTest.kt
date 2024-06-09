package br.ufpe.liber.controllers

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

@MicronautTest
class DefaultExceptionHandlerTest(private val exceptionHandler: DefaultExceptionHandler) :
    BehaviorSpec({
        given("Default ExceptionHandler") {
            `when`("there is a server error") {
                then("it should return 500 Internal Server Error") {
                    val request: HttpRequest<Any> = mockk()
                    val exception = Exception("The error message")

                    every { request.path } answers { "/testing-path" }

                    val response = exceptionHandler.handle(request, exception)
                    response.status.shouldBe(HttpStatus.INTERNAL_SERVER_ERROR)
                    response.header(HttpHeaders.CONTENT_TYPE) shouldBe "text/html; charset=utf-8"
                }
            }
        }
    })
