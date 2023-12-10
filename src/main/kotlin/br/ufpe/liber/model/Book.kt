package br.ufpe.liber.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.NavigableMap
import java.util.Optional
import java.util.TreeMap

@Serializable
data class Book(
    val id: Long,
    val title: String,
    val alternative: String?,
    val creator: String,
    val publisher: String,
    val date: String?,
    val local: String,
    val collection: String,
    val language: String,
    val contributor: String,
    val subject: String?,
    val description: String?,
    val rights: String?,
    val source: String?,
    val text: String,
    val pages: List<Page> = listOf(),
) {
    @Transient
    private val pagesMap: NavigableMap<Long, Page> = TreeMap()

    @Transient
    val pagesSize: Int = pages.size

    init {
        pages.forEach { page ->
            pagesMap[page.id] = page
        }
    }

    fun firstPage(): Page = pages.first()
    fun page(id: Long): Optional<Page> = Optional.ofNullable(pagesMap[id])
    fun nextPage(id: Long): Optional<Page> = Optional.ofNullable(pagesMap.higherEntry(id)?.value)
    fun previousPage(id: Long): Optional<Page> = Optional.ofNullable(pagesMap.lowerEntry(id)?.value)
}
