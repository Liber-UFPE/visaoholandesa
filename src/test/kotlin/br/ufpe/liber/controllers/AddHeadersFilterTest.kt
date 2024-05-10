package br.ufpe.liber.controllers

import br.ufpe.liber.assets.AssetsResolver
import br.ufpe.liber.get
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.http.filter.ServerFilterPhase
import io.micronaut.kotlin.context.getBean
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
@Suppress("CLASS_NAME_INCORRECT")
class AddHeadersFilterTest(
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

    beforeSpec {
        // AssetsResolver initializes a lateinit property used by the view helpers
        context.getBean(AssetsResolver::class.java)
    }

    given("AddHeadersFilter") {
        `when`("a request is made") {
            val response = client.get("/")
            then("add the X-Content-Type-Options header") {
                response.header("X-Content-Type-Options") shouldBe "nosniff"
            }

            then("add Referrer-Policy header") {
                response.header("Referrer-Policy") shouldBe "no-referrer, strict-origin-when-cross-origin"
            }
        }

        `when`("ordering filters") {
            then("it should be the last one") {
                val filter = context.getBean<AddHeadersFilter>()
                filter.order shouldBe ServerFilterPhase.LAST.order()
            }
        }
    }
})
