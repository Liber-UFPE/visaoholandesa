package br.ufpe.liber.controllers

import br.ufpe.liber.shouldContain
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk

@MicronautTest
class ErrorsControllerTest(
    @Suppress("unused")
    private val errorsController: ErrorsController,
) : BehaviorSpec({
    given("ErrorsController") {
        `when`(".defaultNotFound") {
            then("return 404 Not Found") {
                val request: HttpRequest<Any> = mockk()

                every { request.path } answers { "/testing-path" }

                val response = errorsController.defaultNotFound(request)
                response.status.shouldBe(HttpStatus.NOT_FOUND)
                response.body().shouldContain("/testing-path")
            }
        }
    }
})
