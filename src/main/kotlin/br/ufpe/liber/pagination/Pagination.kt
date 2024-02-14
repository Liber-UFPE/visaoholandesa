package br.ufpe.liber.pagination

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

data class PaginationConfig(
    val spotsStart: Int = 2,
    val spotsBeforePage: Int = 2,
    val spotsAfterPage: Int = 2,
    val spotsEnd: Int = 2,
)

data class Pagination(
    val count: Int,
    val currentPage: Int = DEFAULT_FIRST_PAGE,
    val perPage: Int = DEFAULT_PER_PAGE,
    val config: PaginationConfig = PaginationConfig(),
) {
    companion object {
        const val DEFAULT_FIRST_PAGE = 1
        const val DEFAULT_PER_PAGE = 20
    }

    val pages = ceil(count.toDouble() / perPage.toDouble()).toInt()

    // Use min to avoid page > total pages
    // use max to avoid page < zero
    val page = min(
        max(1, currentPage),
        pages,
    )
    val prev: Int? = (page - 1).takeIf { it > 0 }
    val next: Int? = (page + 1).takeIf { it <= pages }
    val from: Int = calculateFrom()
    val to: Int = calculateTo()

    private fun calculateFrom(): Int {
        val pageMultiplier = max(0, page - 1)
        val possibleFrom = pageMultiplier * perPage + 1
        return min(count, possibleFrom)
    }

    private fun calculateTo(): Int {
        return min(count, from + perPage - 1)
    }

    @Suppress("SAY_NO_TO_VAR")
    fun listPages(): List<PaginationItem> {
        val leftGapStart = 1 + config.spotsStart
        val rightGapStart = max(page + config.spotsAfterPage + 1, leftGapStart)

        val rightGapEnd = pages - config.spotsEnd
        val leftGapEnd = min(page - config.spotsBeforePage - 1, rightGapEnd)

        var start = 1
        val series = mutableListOf<PaginationItem>()
        if (leftGapEnd - leftGapStart > 0) {
            series.addAll(rangeAsList(start.rangeUntil(leftGapStart)))
            series.add(Gap)
            start = leftGapEnd + 1
        }

        if (rightGapEnd - rightGapStart > 0) {
            series.addAll(rangeAsList(start.rangeUntil(rightGapStart)))
            series.add(Gap)
            start = rightGapEnd + 1
        }
        series.addAll(rangeAsList(start..pages))
        return series.toList()
    }

    private fun rangeAsList(range: IntRange): List<PaginationItem> = range.map { page ->
        SinglePage(page, current = page == this.page)
    }
}

sealed interface PaginationItem
data class SinglePage(val number: Int, val current: Boolean = false) : PaginationItem {
    override fun toString(): String = number.toString()
}

data object Gap : PaginationItem {
    override fun toString(): String = "..."
}
