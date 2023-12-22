package br.ufpe.liber.controllers

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.kotlin.context.createBean
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import java.util.Optional

@MicronautTest
class SeoControllerTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({

    val client = context.createBean<HttpClient>(server.url).toBlocking()

    given("#sitemap") {
        `when`("GET /sitemap.xml") {
            val response = client.exchange("/sitemap.xml", KteWriteable::class.java)
            then("should return HTTP Ok") {
                response.status() shouldBe HttpStatus.OK
            }

            then("should respond with text/plain") {
                response.contentType shouldBe Optional.of(MediaType.TEXT_XML_TYPE)
            }
        }
    }

    given("#robots") {
        `when`("GET /robots.txt") {
            val response = client.exchange("/robots.txt", KteWriteable::class.java)
            then("should return HTTP Ok") {
                response.status() shouldBe HttpStatus.OK
            }

            then("should respond with text/plain") {
                response.contentType shouldBe Optional.of(MediaType.TEXT_PLAIN_TYPE)
            }

            then("should include sitemap with correct host") {
                val host = server.url
                client.retrieve("/robots.txt") shouldContain "Sitemap: $host/sitemap.xml"
            }
        }
    }
})
