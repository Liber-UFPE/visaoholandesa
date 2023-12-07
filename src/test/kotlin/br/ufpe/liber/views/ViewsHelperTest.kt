package br.ufpe.liber.views

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.optional.shouldNotBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.views.turbo.http.TurboHttpHeaders.TURBO_FRAME
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
        `when`("Turbo-Frame header is present") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[TURBO_FRAME] } answers { "main-navigation-frame" }
                    every { req.headers } answers { headers }
                }
            }

            afterTest {
                clearStaticMockk(ServerRequestContext::class)
            }

            then("should return its value") {
                ViewsHelper.turboFrame().shouldBePresent { value ->
                    value shouldBe "main-navigation-frame"
                }
            }

            then("should be a turbo request") {
                ViewsHelper.isTurboRequest() shouldBe true
                ViewsHelper.notTurboRequest() shouldBe false
            }
        }

        `when`("Turbo-Frame header is NOT present") {
            beforeTest {
                setupCurrentRequest { req ->
                    val headers: HttpHeaders = mockk()
                    every { headers[TURBO_FRAME] } answers { null }
                    every { req.headers } answers { headers }
                }
            }

            then("should return empty value") {
                ViewsHelper.turboFrame().shouldNotBePresent()
            }

            then("it should not be a turbo request") {
                ViewsHelper.isTurboRequest() shouldBe false
                ViewsHelper.notTurboRequest() shouldBe true
            }
        }
    }
})
