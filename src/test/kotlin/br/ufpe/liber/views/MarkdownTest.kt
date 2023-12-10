package br.ufpe.liber.views

import br.ufpe.liber.asString
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MarkdownTest : BehaviorSpec({
    fun renderMarkdown(input: String): String = Markdown.toHtml(input).asString()

    given("Markdown") {
        `when`(".toHtml") {
            then("should render HTML as expected") {
                val input = "This is *markdown*"
                renderMarkdown(input) shouldBe "<p>This is <em>markdown</em></p>"
            }

            then("should render footnotes") {
                val input = """
                    | This is my content.[^fn]
                    |
                    | [^fn]: my footnote
                """.trimMargin()
                renderMarkdown(input) shouldBe """
                    |<p>This is my content.<sup id="fnref-1"><a class="footnote-ref" href="#fn-1">1</a></sup></p>
                    |<div class="footnotes">
                    |<hr />
                    |<ol>
                    |<li id="fn-1">
                    |<p>my footnote</p>
                    |<a href="#fnref-1" class="footnote-backref">&#8617;</a>
                    |</li>
                    |</ol>
                    |</div>
                """.trimMargin()
            }

            then("allow <mark> tags") {
                val input = "This is <mark>Markdown</mark>"
                renderMarkdown(input) shouldBe "<p>This is <mark>Markdown</mark></p>"
            }
        }
    }
})
