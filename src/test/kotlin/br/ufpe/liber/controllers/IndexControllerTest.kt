package br.ufpe.liber.controllers

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class IndexControllerTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({
    val client = context
        .createBean(
            HttpClient::class.java,
            server.url,
            DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
        )
        .toBlocking()

    given("IndexController") {
        `when`("navigating to pages") {
            forAll(
                row("/", HttpStatus.OK),
                row("/does-not-exists", HttpStatus.NOT_FOUND),
            ) { path, expectedStatus ->
                then("GET $path should return $expectedStatus") {
                    val request: HttpRequest<Unit> = HttpRequest.GET(path)
                    client.exchange(
                        request,
                        Argument.of(KteWriteable::class.java),
                        Argument.of(KteWriteable::class.java),
                    ).status.shouldBe(expectedStatus)
                }
            }
        }
    }
})
