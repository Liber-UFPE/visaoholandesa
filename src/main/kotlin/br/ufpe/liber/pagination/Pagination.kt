package br.ufpe.liber.pagination

data class Page(val number: Int, val label: String = "") {

    val hidden: Boolean = (label == Pagination.HIDDEN_SLOTS)

    override fun toString(): String = label.ifBlank { number.toString() }
}

class Pagination(
    val current: Int = FIRST,
    val total: Int,
) {

    companion object {
        const val FIRST = 1
        const val SLOTS_TO_SHOW = 10
        const val HIDDEN_SLOTS = "..."

        // How many pages to show before the current page
        // when in the middle of the pagination, and the
        // first page is not shown.
        private const val BEFORE_CURRENT_IN_THE_MIDDLE: Int = 1

        // How many slots are taken in the end when showing
        // in the middle of the pagination: 1 is for "...",
        // and the other two are used to show the last two pages.
        private const val USED_SLOTS_AT_THE_END: Int = 3

        // The amount of remaining slots to show when navigation
        // is in the middle of the pagination.
        private const val REMAINING_SLOTS: Int =
            SLOTS_TO_SHOW - 1 - BEFORE_CURRENT_IN_THE_MIDDLE - USED_SLOTS_AT_THE_END
    }

    @Suppress("detekt:ReturnCount")
    fun listPages(): List<Page> {
        if (total == 0) return emptyList()
        if (total <= SLOTS_TO_SHOW) return (current..total).map { Page(it) }

        // Check if it is the first page or if the distance to first is very small
        val closeToFirst = (current - FIRST) <= 2
        if (closeToFirst) {
            val result = mutableListOf(Page(0, HIDDEN_SLOTS), Page(total - 1), Page(total))
            val howManyToShow = SLOTS_TO_SHOW - USED_SLOTS_AT_THE_END

            for ((index, page) in (FIRST..howManyToShow).withIndex()) {
                // Add at the index so that we can put the pages at the correct spot
                result.add(index, Page(page))
            }
            return result.toList()
        }

        // Check if it is close to the end
        val closeToEnd = (total - current) < (SLOTS_TO_SHOW - 1)
        if (closeToEnd) {
            // Create the result with and "..." page at the very beggining
            val result = mutableListOf(Page(0, HIDDEN_SLOTS))
            val startOfRange = total - (SLOTS_TO_SHOW - 2) // -2 because the ranges are inclusive
            for (page in (startOfRange..total)) {
                result.add(Page(page))
            }
            return result.toList()
        }

        // Create the result with and "..." page at the very beggining
        val result = mutableListOf(Page(0, HIDDEN_SLOTS))
        // We show the immediate page before current, and a few subsequent pages
        val start = current - BEFORE_CURRENT_IN_THE_MIDDLE
        val end = start + REMAINING_SLOTS
        for (page in start..end) {
            result.add(Page(page))
        }

        // Add "...", and 2 last pages
        result.addAll(listOf(Page(0, HIDDEN_SLOTS), Page(total - 1), Page(total)))

        return result.toList()
    }
}
