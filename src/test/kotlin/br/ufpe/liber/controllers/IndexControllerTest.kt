package br.ufpe.liber.controllers

import br.ufpe.liber.get
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.kotlin.context.createBean
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.views.csp.CspFilter

@MicronautTest
class IndexControllerTest(private val server: EmbeddedServer, private val context: ApplicationContext) :
    BehaviorSpec({
        val client = context.createBean<HttpClient>(
            server.url,
            DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
        ).toBlocking()

        given("IndexController") {
            `when`("navigating to pages") {
                forAll(
                    row("/", HttpStatus.OK),
                    row("/obras", HttpStatus.OK),
                    row("/contato", HttpStatus.OK),
                    row("/equipe", HttpStatus.OK),
                    row("/does-not-exists", HttpStatus.NOT_FOUND),
                ) { path, expectedStatus ->
                    then("GET $path should return $expectedStatus") {
                        val response = client.get(path)
                        response.status shouldBe expectedStatus
                    }

                    then("GET $path should return correct Content-Type") {
                        val response = client.get(path)
                        response.header(HttpHeaders.CONTENT_TYPE) shouldBe "text/html; charset=utf-8"
                    }

                    then("GET $path should include CSP header") {
                        val response = client.get(path)
                        response.header(CspFilter.CSP_HEADER) shouldNotBe null
                    }
                }
            }
        }
    })
