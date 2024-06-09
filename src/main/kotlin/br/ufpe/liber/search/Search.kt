package br.ufpe.liber.search

import br.ufpe.liber.model.Book
import br.ufpe.liber.model.Page
import br.ufpe.liber.pagination.Pagination
import gg.jte.Content
import io.micronaut.context.annotation.Bean
import io.micronaut.core.async.publisher.AsyncSingleResultPublisher
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TopScoreDocCollectorManager
import org.reactivestreams.Publisher
import java.util.concurrent.ExecutorService
import kotlin.math.max
import kotlin.math.min

@Singleton
class Search(
    private val indexSearcher: IndexSearcher,
    private val analyzer: Analyzer,
    private val textHighlighter: TextHighlighter,
) {
    companion object {
        const val MAX_HITS_THRESHOLD: Int = 5_000
    }

    @Suppress("detekt:MemberNameEqualsClassName")
    fun search(keywords: String, page: Int = 0): SearchResults {
        val queryParser = QueryParser(PageMetadata.TEXT, analyzer)
        val query = queryParser.parse(keywords)
        val highlighter = textHighlighter.highlighter(query)

        val storedFields = indexSearcher.storedFields()
        val indexReader = indexSearcher.indexReader
        val termVectors = indexReader.termVectors()

        val collector = TopScoreDocCollectorManager(MAX_HITS_THRESHOLD, MAX_HITS_THRESHOLD + 1)
        val topDocs = indexSearcher.search(query, collector)

        if (topDocs.totalHits.value == 0L) {
            return SearchResults.empty()
        }

        val pagingStart: Int = max(page, 0) * Pagination.DEFAULT_PER_PAGE
        val pagingEnd: Int = min(pagingStart + Pagination.DEFAULT_PER_PAGE, topDocs.totalHits.value.toInt()) - 1

        val searchResults = topDocs.scoreDocs.slice(pagingStart..pagingEnd).map { scoreDoc ->
            val document = storedFields.document(scoreDoc.doc)
            val pageContents = document[PageMetadata.TEXT]

            val fields = termVectors[scoreDoc.doc]
            val highlightedContent = textHighlighter.highlightContent(highlighter, pageContents, fields)

            SearchResult(document, highlightedContent)
        }

        return SearchResults(topDocs.totalHits.value.toInt(), searchResults, page + 1)
    }
}

@Serdeable
data class SearchResult(val book: Book, val page: Page, val highlightedContent: Content) {
    constructor(doc: Document, highlightedContent: Content) : this(
        Book(
            id = doc[BookMetadata.ID].toLong(),
            title = doc[BookMetadata.TITLE],
            alternative = doc[BookMetadata.ALTERNATIVE],
            creator = doc[BookMetadata.CREATOR],
            publisher = doc[BookMetadata.PUBLISHER],
            date = doc[BookMetadata.DATE],
            local = doc[BookMetadata.LOCAL],
            collection = doc[BookMetadata.COLLECTION],
            language = doc[BookMetadata.LANGUAGE],
            contributor = doc[BookMetadata.CONTRIBUTOR],
            subject = doc[BookMetadata.SUBJECT],
            description = doc[BookMetadata.DESCRIPTION],
            rights = doc[BookMetadata.RIGHTS],
            source = doc[BookMetadata.SOURCE],
            text = doc[BookMetadata.TEXT],
        ),
        Page(
            id = doc[PageMetadata.ID].toLong(),
            number = doc[PageMetadata.NUMBER].toLong(),
            text = doc[PageMetadata.TEXT],
        ),
        highlightedContent,
    )
}

@Serdeable
data class SearchResults(
    val hits: Int = 0,
    val items: List<SearchResult> = listOf(),
    val currentPage: Int = Pagination.DEFAULT_FIRST_PAGE,
) : Collection<SearchResult> by items {
    companion object {
        fun empty() = SearchResults()
    }

    val pagination = Pagination(count = hits, currentPage = currentPage)
}

@Bean
class SearchHealthIndicator(
    private val search: Search,
    @Named(TaskExecutors.BLOCKING) private val executorService: ExecutorService,
) : HealthIndicator {
    companion object {
        private const val DEFAULT_TEST_SEARCH = "recife"
        private val UP = HealthStatus(HealthStatus.NAME_UP, "Search is operational", true, null)
        private val DOWN = HealthStatus(HealthStatus.NAME_DOWN, "Search is NOT operational", false, null)
    }

    override fun getResult(): Publisher<HealthResult> = AsyncSingleResultPublisher(executorService, ::getHealthResult)

    @Suppress("detekt:TooGenericExceptionCaught")
    private fun getHealthResult(): HealthResult {
        val builder = HealthResult.builder("search")
        return try {
            search.search(DEFAULT_TEST_SEARCH)
            builder.status(UP).build()
        } catch (ex: Exception) {
            builder.exception(ex).status(DOWN).build()
        }
    }
}
