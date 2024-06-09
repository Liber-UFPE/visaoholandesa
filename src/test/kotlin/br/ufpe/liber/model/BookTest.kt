package br.ufpe.liber.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.micronaut.core.io.ResourceResolver
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.serialization.json.Json
import java.util.Optional

@Suppress("detekt:MaxLineLength", "LONG_LINE")
@MicronautTest
class BookTest(private val resourceResolver: ResourceResolver) :
    BehaviorSpec({
        given("Book loaded from JSON") {
            val book: Book =
                resourceResolver
                    .getResource("classpath:data/json/book-1.json")
                    .map { url -> Json.decodeFromString<Book>(url.readText()) }
                    .get()

            `when`("decoding the model") {
                then("should have the correct id") { book.id shouldBe 1 }
                then("should have the correct title") {
                    book.title shouldBe "A Igreja Cristã Reformada no Brasil Holandês"
                }
                then("should have the correct alternative") {
                    book.alternative shouldBe
                        "Atas das Assembléias clássicais da Igreja Cristã Reformada no Brasil holandês"
                }
                then("should have the correct creator") { book.creator shouldBe "Frans Leonard Schalkwijk" }
                then("should have the correct publisher") {
                    book.publisher shouldBe "Instituto Arqueológico Histórico e Geográfico Pernambucano"
                }
                then("should have the correct date") { book.date shouldBe "1993" }
                then("should have the correct local") { book.local shouldBe "Recife" }

                then("should have the correct collection") {
                    book.collection shouldBe
                        "Revista do Instituto Arqueológico Histórico e Geográfico Pernambucano, volume LVIII, pp 145-284."
                }
                then("should have the correct language") { book.language shouldBe "Português" }
                then("should have the correct contributor") { book.contributor shouldBe "Frans Leonard Schalkwijk" }
                then("should have the correct subject") { book.subject shouldBe null }
                then("should have the correct description") {
                    book.description shouldStartWith
                        "Tradução e publicação das Atas das Assembléias da Igreja Cristã Reformada no Brasil holandês"
                }
                then("should have the correct rights") {
                    book.rights shouldBe
                        "Propriedade intelectual; Copyright - Liber"
                }
                then("should have the correct source") {
                    book.source shouldBe
                        "Revista do Instituto Arqueológico Histórico e Geográfico Pernambucano revisado pelo tradutor."
                }
                then("should have the correct text") { book.text shouldBe "Artigo" }

                then("should have the correct firt page") {
                    val page = book.pages.first()
                    page.id shouldBe 1
                    page.number shouldBe 145
                }

                then("should have the correct last page") {
                    val page = book.pages.last()
                    page.id shouldBe 138
                    page.number shouldBe 283
                }

                then("pages should be sorted by id") {
                    book.pages shouldBeSortedWith { page1, page2 -> (page1.id - page2.id).toInt() }
                }

                then("pages should be sorted by number") {
                    book.pages shouldBeSortedWith { page1, page2 -> (page1.number - page2.number).toInt() }
                }
            }

            `when`(".pagesSize") {
                then("page size is correctly calcuated") { book.pagesSize shouldBe 138 }
            }

            `when`(".page") {
                then("should return a page when id exists") {
                    book.page(book.pages.first().id) shouldBePresent {
                        it.id shouldBe 1
                        it.number shouldBe 145
                    }
                }

                then("should return an empty optional when id does not exist") {
                    // We know that there are no negative IDs
                    book.page(-1L) shouldBe Optional.empty()
                }
            }

            `when`(".nextPage") {
                then("get page after first page") {
                    book.nextPage(book.pages.first().id) shouldBePresent {
                        it.id shouldBe 2
                        it.number shouldBe 147
                    }
                }

                then("some page in the middle should have next page") {
                    val pageId: Long = 9
                    book.nextPage(pageId) shouldBePresent {
                        it.id shouldBe 10
                        it.number shouldBe 155
                    }
                }

                then("next page after last page should be empty") {
                    book.nextPage(book.pages.last().id) shouldBe Optional.empty()
                }
            }

            `when`(".previousPage") {
                then("get page before last page") {
                    book.previousPage(book.pages.last().id) shouldBePresent {
                        it.id shouldBe 137
                        it.number shouldBe 282
                    }
                }

                then("some page in the middle should have previous page") {
                    val pageId: Long = 9
                    book.previousPage(pageId) shouldBePresent {
                        it.id shouldBe 8
                        it.number shouldBe 153
                    }
                }

                then("previous page before first page should be empty") {
                    book.previousPage(book.pages.first().id) shouldBe Optional.empty()
                }
            }
        }
    })
