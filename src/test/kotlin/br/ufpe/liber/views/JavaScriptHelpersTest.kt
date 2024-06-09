package br.ufpe.liber.views

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class JavaScriptHelpersTest :
    BehaviorSpec({
        given("JavaScriptHelpers") {
            `when`(".jsonLdEncode") {
                then("should scapes double quotes") {
                    JavaScriptHelpers.jsonLdEncode("Has \"double quotes\"") shouldBe "Has \\\"double quotes\\\""
                }

                then("should scape single quotes") {
                    JavaScriptHelpers.jsonLdEncode("Has 'single quotes'") shouldBe "Has \\\'single quotes\\\'"
                }

                then("should scape... scape sequences") {
                    forAll(
                        row("The scape sequence \n is scaped", "The scape sequence \\n is scaped"),
                        row("The scape sequence \t is scaped", "The scape sequence \\t is scaped"),
                        row("The scape sequence \r is scaped", "The scape sequence \\r is scaped"),
                        row("The scape sequence \\f is scaped", "The scape sequence \\\\f is scaped"),
                        row("The scape sequence \b is scaped", "The scape sequence \\b is scaped"),
                    ) { input, expected -> JavaScriptHelpers.jsonLdEncode(input) shouldBe expected }
                }

                then("should scape / char") {
                    JavaScriptHelpers.jsonLdEncode("/ should be scaped") shouldBe "\\/ should be scaped"
                }

                then("should NOT scape - char") {
                    JavaScriptHelpers.jsonLdEncode("Do not scape -") shouldBe "Do not scape -"
                }
            }
        }
    })
