package br.ufpe.liber.views

import br.ufpe.liber.asString
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.views.htmx.http.HtmxRequestHeaders
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.Optional

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

class ViewsHelperTest : BehaviorSpec({
    fun setupCurrentRequest(request: Optional<HttpRequest<Any>>) {
        mockkStatic(ServerRequestContext::class)
        every { ServerRequestContext.currentRequest<Any>() } answers { request }
    }

    fun setupCurrentRequest(block: (req: HttpRequest<Any>) -> Unit) {
        val request: HttpRequest<Any> = mockk()
        block(request)
        setupCurrentRequest(Optional.of(request))
    }

    given("ViewHelpers") {
        `when`("HX-Request header value is \"true\"") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[HtmxRequestHeaders.HX_REQUEST] } answers { "true" }
                    every { req.headers } answers { headers }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("should be a htmx request") {
                ViewsHelper.isHtmxRequest() shouldBe true
                ViewsHelper.notHtmxRequest() shouldBe false
            }
        }

        `when`("HX-Request header is NOT present") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[HtmxRequestHeaders.HX_REQUEST] } answers { null }
                    every { req.headers } answers { headers }
                }
            }

            then("it should not be a htmx request") {
                ViewsHelper.isHtmxRequest() shouldBe false
                ViewsHelper.notHtmxRequest() shouldBe true
            }
        }

        `when`(".isActive") {
            beforeTest {
                setupCurrentRequest { req ->
                    every { req.path } answers { "/contact" }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("it should be marked as active when path matches") {
                ViewsHelper.isActive("/contact") shouldBe true
            }

            then("it should not be marked as active when path does NOT match") {
                ViewsHelper.isActive("/about") shouldBe false
            }
        }

        `when`(".emptyContent") {
            then("there is nothing to render") {
                ViewsHelper.emptyContent().asString() shouldBe ""
            }

            then("it should be marked as emptyContent") {
                ViewsHelper.emptyContent().isEmptyContent shouldBe true
            }
        }
    }
})
