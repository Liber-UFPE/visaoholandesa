package br.ufpe.liber.views

import br.ufpe.liber.asString
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.optional.shouldNotBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.Optional

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
        `when`("HX-Target header is present") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[ViewsHelper.HX_TARGET_HEADER] } answers { "main-content" }
                    every { req.headers } answers { headers }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("should return its value") {
                ViewsHelper.htmxTarget().shouldBePresent { value ->
                    value.first shouldBe ViewsHelper.HX_TARGET_HEADER
                    value.second shouldBe "main-content"
                }
            }
        }

        `when`("HX-Target header is NOT present") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[ViewsHelper.HX_TARGET_HEADER] } answers { null }
                    every { req.headers } answers { headers }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("should return empty value") {
                ViewsHelper.htmxTarget().shouldNotBePresent()
            }
        }

        `when`("HX-Request header value is \"true\"") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[ViewsHelper.HX_REQUEST_HEADER] } answers { "true" }
                    every { req.headers } answers { headers }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("should return its value") {
                ViewsHelper.htmxRequest().shouldBePresent { value ->
                    value.first shouldBe ViewsHelper.HX_REQUEST_HEADER
                    value.second shouldBe true
                }
            }

            then("should be a turbo request") {
                ViewsHelper.isHtmxRequest() shouldBe true
                ViewsHelper.notHtmxRequest() shouldBe false
            }
        }

        `when`("HX-Request header value is \"false\"") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[ViewsHelper.HX_REQUEST_HEADER] } answers { "false" }
                    every { req.headers } answers { headers }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("should return its value") {
                ViewsHelper.htmxRequest().shouldBePresent { value ->
                    value.first shouldBe ViewsHelper.HX_REQUEST_HEADER
                    value.second shouldBe false
                }
            }

            then("should be a turbo request") {
                ViewsHelper.isHtmxRequest() shouldBe false
                ViewsHelper.notHtmxRequest() shouldBe true
            }
        }

        `when`("Turbo-Frame header is NOT present") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[ViewsHelper.HX_REQUEST_HEADER] } answers { null }
                    every { req.headers } answers { headers }
                }
            }

            then("should return empty value") {
                ViewsHelper.htmxRequest().shouldNotBePresent()
            }

            then("it should not be a turbo request") {
                ViewsHelper.isHtmxRequest() shouldBe false
                ViewsHelper.notHtmxRequest() shouldBe true
            }
        }

        `when`(".emptyContent") {
            then("there is nothing to render") {
                ViewsHelper.emptyContent().asString() shouldBe ""
            }
        }
    }
})
