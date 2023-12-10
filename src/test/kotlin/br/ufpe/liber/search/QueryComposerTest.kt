package br.ufpe.liber.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class QueryComposerTest : BehaviorSpec({
    given("QueryComposer") {
        `when`("includes 'all words'") {
            then("should format using +") {
                val query = "recife olinda"
                val composer = QueryComposer(allWords = query)
                composer.query shouldBe "+recife +olinda"
            }

            then("should clean up stop words") {
                val query = "recife olinda mas com algumas palavras para descartar"
                val composer = QueryComposer(allWords = query)
                composer.query shouldBe "+recife +olinda +algumas +palavras +para +descartar"
            }
        }

        `when`("includes 'one of words'") {
            then("no special operators are added") {
                val query = "recife olinda"
                val composer = QueryComposer(oneOfWords = query)
                composer.query shouldBe "recife olinda"
            }

            then("should clean up stop words") {
                val query = "recife olinda mas com algumas palavras para descartar"
                val composer = QueryComposer(oneOfWords = query)
                composer.query shouldBe "recife olinda algumas palavras para descartar"
            }
        }

        `when`("includes 'exact phrase'") {
            then("should wrap value with double quotes") {
                val query = "recife olinda"
                val composer = QueryComposer(exactPhrase = query)
                composer.query shouldBe "\"recife olinda\""
            }

            then("should clean up stop words") {
                val query = "recife com olinda"
                val composer = QueryComposer(exactPhrase = query)
                composer.query shouldBe "\"recife olinda\""
            }
        }

        `when`("includes 'not words'") {
            then("should format using -") {
                val query = "recife olinda"
                val composer = QueryComposer(notWords = query)
                composer.query shouldBe "-recife -olinda"
            }

            then("should clean up stop words") {
                val query = "recife olinda mas com algumas palavras para descartar"
                val composer = QueryComposer(notWords = query)
                composer.query shouldBe "-recife -olinda -algumas -palavras -para -descartar"
            }
        }

        `when`("all values are empty") {
            then("return empty query") {
                QueryComposer().query shouldBe ""
            }
        }

        `when`("mixing multiple criteria") {
            forAll(
                row(
                    /* testDescription = */
                    "'all words' and 'one of words",
                    /* allWords = */
                    "recife e olinda",
                    /* oneOfWords = */
                    "bairro e lugar",
                    /* exactPhrase = */
                    "",
                    /* notWords = */
                    "",
                    /* expectedResult = */
                    "+recife +olinda bairro lugar",
                ),
                row(
                    /* testDescription = */
                    "'all words' and 'not words",
                    /* allWords = */
                    "recife e olinda",
                    /* oneOfWords = */
                    "",
                    /* exactPhrase = */
                    "",
                    /* notWords = */
                    "bairro e lugar",
                    /* expectedResult = */
                    "+recife +olinda -bairro -lugar",
                ),
                row(
                    /* testDescription = */
                    "'all words' and 'exact phrase",
                    /* allWords = */
                    "recife e olinda",
                    /* oneOfWords = */
                    "",
                    /* exactPhrase = */
                    "bairro e lugar",
                    /* notWords = */
                    "",
                    /* expectedResult = */
                    "+recife +olinda \"bairro lugar\"",
                ),
                row(
                    /* testDescription = */
                    "'all words', 'one of words, and 'not words",
                    /* allWords = */
                    "recife e olinda",
                    /* oneOfWords = */
                    "bairro e lugar",
                    /* exactPhrase = */
                    "",
                    /* notWords = */
                    "palavras descartáveis",
                    /* expectedResult = */
                    "+recife +olinda bairro lugar -palavras -descartáveis",
                ),
                row(
                    /* testDescription = */
                    "'all words', 'one of words, 'exact phrase', and 'not words",
                    /* allWords = */
                    "recife e olinda",
                    /* oneOfWords = */
                    "bairro e lugar",
                    /* exactPhrase = */
                    "buscar texto",
                    /* notWords = */
                    "palavras descartáveis",
                    /* expectedResult = */
                    "+recife +olinda bairro lugar \"buscar texto\" -palavras -descartáveis",
                ),
            ) { testDescription, allWords, oneOfWords, exactPhrase, notWords, expectedResult ->
                then("test: $testDescription") {
                    val composer = QueryComposer(allWords, oneOfWords, exactPhrase, notWords)
                    composer.query shouldBe expectedResult
                }
            }
        }

        `when`(".empty") {
            val emptyQueryComposer = QueryComposer.empty()
            then("allWords is blank") { emptyQueryComposer.allWords shouldBe "" }
            then("oneOfWords is blank") { emptyQueryComposer.oneOfWords shouldBe "" }
            then("exactPhrase is blank") { emptyQueryComposer.exactPhrase shouldBe "" }
            then("notWords is blank") { emptyQueryComposer.notWords shouldBe "" }
            then("isEmpty should return true") { emptyQueryComposer.isEmpty() shouldBe true }
            then("isEmpty should return false when one of the fields is not empty") {
                QueryComposer(allWords = "something").isEmpty() shouldBe false
                QueryComposer(oneOfWords = "something").isEmpty() shouldBe false
                QueryComposer(exactPhrase = "something").isEmpty() shouldBe false
                QueryComposer(notWords = "something").isEmpty() shouldBe false
            }
        }
    }
})
