package br.ufpe.liber

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.micronaut.context.env.Environment
import io.mockk.every
import io.mockk.mockk

class TemplatesFactoryTest :
    BehaviorSpec({
        val factory = TemplatesFactory()
        given("createTemplate") {
            `when`("it is dev environment") {
                val environment = mockk<Environment>()
                every { environment.activeNames } answers { setOf("something", Environment.DEVELOPMENT) }

                then("it should use dynamic templates") {
                    val templates = factory.createTemplate(environment)
                    (templates is DynamicTemplates) shouldBe true
                }
            }

            forAll(
                row(Environment.TEST),
                row(Environment.CLI),
                row(Environment.CLOUD),
                row(Environment.BARE_METAL),
            ) { envName ->
                `when`("it is $envName environment") {
                    val environment = mockk<Environment>()
                    every { environment.activeNames } answers { setOf(envName) }

                    then("it should use dynamic templates") {
                        val templates = factory.createTemplate(environment)
                        (templates is StaticTemplates) shouldBe true
                    }
                }
            }
        }
    })
