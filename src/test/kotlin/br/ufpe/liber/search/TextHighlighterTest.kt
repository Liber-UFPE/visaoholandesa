package br.ufpe.liber.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class TextHighlighterTest :
    BehaviorSpec({
        val analyzer = SearchFactory().createAnalyzer()
        beforeSpec {
            TextHighlighter.staticAnalyzer = analyzer
        }

        given("static #highlightText") {
            `when`("there is a query and text") {
                then("it should highlight the query") {
                    val query = "Recife"
                    val text = "A capital de Pernambuco, Recife, é conhecida com a Veneza brasileira"
                    TextHighlighter.highlightText(query, text) shouldContain "<mark>Recife</mark>"
                }
            }

            `when`("query has accented words") {
                then("should highlight the accented words") {
                    val query = "assistência"
                    val text = "Ela precisará de assistência quando estiver em Recife"
                    TextHighlighter.highlightText(
                        query,
                        text,
                    ) shouldContain "Ela precisará de <mark>assistência</mark> quando estiver em Recife"
                }
            }

            `when`("query is empty") {
                then("should return the text as is") {
                    val text = "A capital de Pernambuco, Recife, é conhecida com a Veneza brasileira"
                    TextHighlighter.highlightText("", text) shouldBe text
                }
            }

            `when`("query has stop words") {
                then("should not highlight the stop words") {
                    val query = "a capital de pernambuco"
                    val text = "Em Recife, a capital de Pernambuco"
                    TextHighlighter.highlightText(
                        query,
                        text,
                    ) shouldContain "Em Recife, a <mark>capital</mark> de <mark>Pernambuco</mark>"
                }
            }

            `when`("query only have stop words") {
                forAll(
                    row("nesse"),
                    row("ou esse"),
                    row("e também"),
                ) { query ->
                    val text = "A capital de Pernambuco, Recife, é conhecida com a Veneza brasileira"
                    then("should return the text as is for query '$query'") {
                        TextHighlighter.highlightText(query, text) shouldBe text
                    }
                }
            }
        }
    })
