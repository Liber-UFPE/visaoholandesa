package br.ufpe.liber.pagination

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class PaginationTest : BehaviorSpec({
    given("Page") {
        `when`(".toString()") {
            then("show label if not blank") {
                Page(number = 1, label = "First").toString() shouldBe "First"
            }
            then("show number if label blank") {
                Page(number = 1).toString() shouldBe "1"
            }
        }
        `when`(".hidden") {
            then("true when label is '...'") {
                Page(10, label = "...").hidden shouldBe true
            }

            then("false when label is empty") {
                Page(10).hidden shouldBe false
            }

            then("false when label is another value") {
                Page(10, label = "Ten").hidden shouldBe false
            }
        }
    }

    given("Pages") {
        `when`(".listPages") {
            forAll(
                // current, total, expected
                row(1, 5, (1..5).map { it.toString() }),
                row(1, 11, listOf("1", "2", "3", "4", "5", "6", "7", "...", "10", "11")),
                row(2, 11, listOf("1", "2", "3", "4", "5", "6", "7", "...", "10", "11")),
                row(3, 11, listOf("1", "2", "3", "4", "5", "6", "7", "...", "10", "11")),
                row(5, 11, listOf("...", "3", "4", "5", "6", "7", "8", "9", "10", "11")),
                row(1, 100, listOf("1", "2", "3", "4", "5", "6", "7", "...", "99", "100")),
                row(5, 100, listOf("...", "4", "5", "6", "7", "8", "9", "...", "99", "100")),
                row(35, 100, listOf("...", "34", "35", "36", "37", "38", "39", "...", "99", "100")),
                row(91, 100, listOf("...", "90", "91", "92", "93", "94", "95", "...", "99", "100")),
                row(99, 100, listOf("...", "92", "93", "94", "95", "96", "97", "98", "99", "100")),
            ) { current, total, expectedResult ->
                then("when current = $current, and total pages = $total") {
                    Pagination(current, total).listPages().map { it.toString() } shouldBe expectedResult
                }
            }
        }
    }
})
