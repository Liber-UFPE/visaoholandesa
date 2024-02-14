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
class ErrorsControllerTest(private val errorsController: ErrorsController) : BehaviorSpec({
    given("ErrorsController") {
        `when`(".defaultNotFound") {
            then("return 404 Not Found") {
                val request: HttpRequest<Any> = mockk()

                every { request.path } answers { "/testing-path" }

                val response = errorsController.defaultNotFound(request)
                response.status.shouldBe(HttpStatus.NOT_FOUND)
                response.header(HttpHeaders.CONTENT_TYPE) shouldBe "text/html; charset=utf-8"
            }
        }
    }
})
