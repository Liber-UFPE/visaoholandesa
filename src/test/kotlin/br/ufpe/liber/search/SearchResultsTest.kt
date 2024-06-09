package br.ufpe.liber.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

class SearchResultsTest :
    BehaviorSpec({
        given("SearchResults") {
            `when`(".empty()") {
                then("hits should be zero") { SearchResults.empty().hits shouldBe 0 }
                then("items should be empty") { SearchResults.empty().items shouldBe emptyList() }
                then("current page is 1") { SearchResults.empty().currentPage shouldBe 1 }
            }

            forAll(
                table(
                    headers("hits", "current page", "expected total pages"),
                    row(103, 2, 6),
                ),
            ) { hits, currentPage, expectedTotalPages ->
                `when`("there are $hits hits") {
                    then("should create pagination object correctly") {
                        val results = SearchResults(hits = hits, currentPage = currentPage)
                        results.pagination.let { pag ->
                            pag.pages shouldBe expectedTotalPages
                            pag.currentPage shouldBe currentPage
                        }
                    }
                }
            }
        }
    })
