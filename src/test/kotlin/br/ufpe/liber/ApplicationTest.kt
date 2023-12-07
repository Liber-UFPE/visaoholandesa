package br.ufpe.liber

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class ApplicationTest(private val application: EmbeddedApplication<*>) : BehaviorSpec({
    given("EmbeddedApplication") {
        `when`("application starts") {
            then("server is running") { application.isRunning shouldBe true }
        }
    }
})
