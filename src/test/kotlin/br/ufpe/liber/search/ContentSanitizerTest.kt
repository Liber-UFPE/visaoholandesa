package br.ufpe.liber.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class ContentSanitizerTest(contentSanitizer: ContentSanitizer) : BehaviorSpec({
    given("ContentSanitizer") {
        `when`(".sanitize") {
            forAll(
                // tagName, input
                row("p", "Some <p>HTML</p> content"),
                row("br", "Some <br />HTML content"),
                row("em", "Some <em>HTML</em> content"),
                row("strong", "Some <strong>HTML</strong> content"),
            ) { tagName, input ->
                then("should remove tag <$tagName>") {
                    contentSanitizer.sanitize(input) shouldBe "Some HTML content"
                }
            }

            then("should not remove <mark> tag") {
                val input = "Some <mark>HTML</mark> content"
                contentSanitizer.sanitize(input) shouldBe "Some <mark>HTML</mark> content"
            }

            then("should close unclosed tags") {
                val input = "<div>Some <mark>HTML content"
                contentSanitizer.sanitize(input) shouldBe "<div>Some <mark>HTML content</mark></div>"
            }
        }
    }
})
