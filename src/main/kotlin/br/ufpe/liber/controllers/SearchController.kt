package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import br.ufpe.liber.search.QueryComposer
import br.ufpe.liber.search.Search
import br.ufpe.liber.search.SearchResults
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import org.apache.lucene.queryparser.classic.ParseException
import java.util.Optional
import kotlin.math.max

@Controller
@Produces(MediaType.TEXT_HTML)
class SearchController(private val search: Search, private val templates: Templates) : KteController {
    @Get("/search")
    fun search(@QueryValue query: Optional<String>, @QueryValue page: Optional<Int>): HttpResponse<KteWriteable> {
        return runSearch(query.orElse(""), page)
    }

    @Get("/advanced-search")
    fun advancedSearch(
        @QueryValue allWords: Optional<String>,
        @QueryValue oneOfWords: Optional<String>,
        @QueryValue exactPhrase: Optional<String>,
        @QueryValue notWords: Optional<String>,
        @QueryValue page: Optional<Int>,
    ): HttpResponse<KteWriteable> {
        val composer = QueryComposer(
            allWords = allWords.orElse(""),
            oneOfWords = oneOfWords.orElse(""),
            exactPhrase = exactPhrase.orElse(""),
            notWords = notWords.orElse(""),
        )
        if (composer.isEmpty()) {
            return ok(templates.advancedSearch())
        }
        return runSearch(composer.query, page)
    }

    private fun runSearch(query: String, page: Optional<Int>): HttpResponse<KteWriteable> {
        if (query.isBlank()) return ok(templates.search(query, SearchResults.empty()))

        // For users, we show 1-based pages, but internally we handle it as 0-based.
        // Therefore, we need to subtract `1`. We `max` it with zero to avoid negative
        // values here.
        val currentPage = page.map { max(it - 1, 0) }.orElse(0)
        val searchResults = try {
            search.search(query, currentPage)
        } catch (ex: ParseException) {
            SearchResults.empty()
        }
        return ok(templates.search(query, searchResults))
    }
}
