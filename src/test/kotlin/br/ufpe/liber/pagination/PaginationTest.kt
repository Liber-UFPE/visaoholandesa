package br.ufpe.liber.pagination

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

class PaginationTest :
    BehaviorSpec({
        given(".pages") {
            forAll(
                table(
                    headers("count", "page", "perPage", "expected amount of pages"),
                    row(103, 1, 20, 6),
                    row(99, 1, 20, 5),
                    row(1, 1, 20, 1),
                    row(20, 1, 20, 1),
                    row(21, 1, 20, 2),
                    row(20, 1, 1, 20),
                    row(21, 1, 1, 21),
                ),
            ) { count, page, perPage, expectedAmountOfPages ->
                `when`("$count items, and $perPage per page") {
                    then("should have $expectedAmountOfPages pages") {
                        Pagination(count, page, perPage).pages shouldBe expectedAmountOfPages
                    }
                }
            }
        }

        given(".page") {
            forAll(
                table(
                    headers("count", "current page", "perPage", "expected page"),
                    row(103, 1, 20, 1),
                    row(103, 3, 20, 3),
                    row(103, -5, 20, 1),
                    row(103, 60, 20, 6),
                ),
            ) { count, currentPage, perPage, page ->
                `when`("$count items, and current page is $page") {
                    then("page should be $page") {
                        Pagination(count, currentPage, perPage).page shouldBe page
                    }
                }
            }
        }

        given(".next") {
            forAll(
                table(
                    headers("count", "page", "perPage", "expected next page"),
                    row(103, 1, 20, 2),
                    row(103, 3, 20, 4),
                    row(103, 5, 20, 6),
                    row(103, 6, 20, null),
                ),
            ) { count, page, perPage, next ->
                `when`("$count items, and current page is $page") {
                    then("next page should be $next") {
                        Pagination(count, page, perPage).next shouldBe next
                    }
                }
            }
        }

        given(".prev") {
            forAll(
                table(
                    headers("count", "page", "perPage", "expected prev page"),
                    row(103, 1, 20, null),
                    row(103, 3, 20, 2),
                    row(103, 5, 20, 4),
                    row(103, 6, 20, 5),
                ),
            ) { count, page, perPage, prev ->
                `when`("$count items, and current page is $page") {
                    then("prev page should be $prev") {
                        Pagination(count, page, perPage).prev shouldBe prev
                    }
                }
            }
        }

        given(".from and .to") {
            forAll(
                table(
                    headers("count", "page", "perPage", "expected from", "expected to"),
                    row(103, 1, 20, 1, 20),
                    row(103, 3, 20, 41, 60),
                    row(103, 5, 20, 81, 100),
                    row(103, 6, 20, 101, 103),
                ),
            ) { count, page, perPage, expectedFrom, expectedTo ->
                `when`("$count items, and current page is $page") {
                    then("`from` should be $expectedFrom") {
                        Pagination(count, page, perPage).from shouldBe expectedFrom
                    }

                    then("`to` should be $expectedTo") {
                        Pagination(count, page, perPage).to shouldBe expectedTo
                    }
                }
            }
        }

        given(".listPages") {
            forAll(
                row(103, 2, listOf("1", "2", "3", "4", "5", "6")),
                row(503, 1, listOf("1", "2", "3", "...", "25", "26")),
                row(503, 2, listOf("1", "2", "3", "4", "...", "25", "26")),
                row(503, 3, listOf("1", "2", "3", "4", "5", "...", "25", "26")),
                row(503, 4, listOf("1", "2", "3", "4", "5", "6", "...", "25", "26")),
                row(503, 12, listOf("1", "2", "...", "10", "11", "12", "13", "14", "...", "25", "26")),
                row(503, 23, listOf("1", "2", "...", "21", "22", "23", "24", "25", "26")),
                row(503, 24, listOf("1", "2", "...", "22", "23", "24", "25", "26")),
                row(503, 25, listOf("1", "2", "...", "23", "24", "25", "26")),
                row(503, 26, listOf("1", "2", "...", "24", "25", "26")),
            ) { count, currentPage, expectedSeries ->
                `when`("count = $count, current page = $currentPage") {
                    then("show pages $expectedSeries") {
                        val pagination = Pagination(count, currentPage)
                        pagination.listPages().map { it.toString() } shouldBe expectedSeries
                    }

                    then("correctly tag current page") {
                        val pagination = Pagination(103, 2)
                        val expectedCurrentPage =
                            pagination.listPages().first { page -> page is SinglePage && page.current } as SinglePage
                        expectedCurrentPage.number shouldBe 2
                    }
                }
            }
        }
    })
