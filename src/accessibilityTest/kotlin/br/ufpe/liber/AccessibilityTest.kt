package br.ufpe.liber

import com.deque.html.axecore.selenium.AxeBuilder
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.kotlin.context.getBean
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.web.router.Router
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

@MicronautTest
class AccessibilityTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({
    val axeBuilder = AxeBuilder().disableRules(listOf("heading-order"))
    val driver = ChromeDriver(
        ChromeOptions()
            .addArguments("--no-sandbox")
            .addArguments("--disable-dev-shm-usage")
            .addArguments("--headless"),
    )

    val parameterlessRoutes = context.getBean<Router>()
        .uriRoutes()
        // filter in only routes that DO NOT require a parameter
        .filter { it.httpMethod == HttpMethod.GET && it.uriMatchTemplate.variables.isEmpty() }
        // filter in only UI routes
        .filter { it.produces.contains(MediaType.TEXT_HTML_TYPE) }
        // get only the paths
        .map { it.uriMatchTemplate.toPathString() }
        // More readable?
        .sorted()
        // since `uriRoutes` returns a `Stream`.
        .toList()

    beforeSpec {
        // For possible initializations since the application will
        // be tested from the users perspective.
    }

    // Called once per Spec, after all tests have completed for that spec.
    finalizeSpec {
        driver.close()
    }

    fun url(path: String): String = "http://${server.host}:${server.port}$path"

    fun checkAccessibility(path: String) {
        driver.get(url(path))
        driver.title shouldNotContain "404"
        driver.title shouldNotContain "500"
        val results = axeBuilder.analyze(driver)
        results.violations shouldBe listOf()
    }

    given("The server is running") {
        `when`("accessing the main pages") {
            @Suppress("detekt:SpreadOperator")
            forAll(*parameterlessRoutes.map(::row).toTypedArray()) { path ->
                then("$path passes accessibility tests") {
                    checkAccessibility(path)
                }
            }
        }
    }
})
