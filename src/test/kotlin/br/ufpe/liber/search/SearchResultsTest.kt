package br.ufpe.liber.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class SearchResultsTest : BehaviorSpec({

    given("SearchResults") {
        `when`(".empty()") {
            then("hits should be zero") { SearchResults.empty().hits shouldBe 0 }
            then("items should be empty") { SearchResults.empty().items shouldBe emptyList() }
            then("current page is 1") { SearchResults.empty().currentPage shouldBe 1 }
        }

        `when`(".totalPages") {
            forAll(
                // hits, expectedPages
                row(0, 0),
                row(1, 1),
                row(10, 1),
                row(22, 3),
                row(831, 84),
                row(1234, 124),
            ) { hits, expectedPages ->
                then("$hits hits should result in $expectedPages pages") {
                    SearchResults(hits).totalPages shouldBe expectedPages
                }
            }
        }

        `when`(".isFirstPage") {
            forAll(
                // current page, expectedResult
                row(1, true),
                row(2, false),
                row(0, false),
            ) { currentPage, expectedResult ->
                then("when current page is $currentPage then isFirstPage $expectedResult") {
                    SearchResults(currentPage = currentPage).isFirstPage shouldBe expectedResult
                }
            }
        }

        `when`(".isLastPage") {
            forAll(
                // hits, current page, expectedResult
                row(10, 1, true),
                row(30, 2, false),
            ) { hits, currentPage, expectedResult ->
                then("when current page is $currentPage then isFirstPage $expectedResult") {
                    SearchResults(hits = hits, currentPage = currentPage).isLastPage shouldBe expectedResult
                }
            }
        }
    }
})
