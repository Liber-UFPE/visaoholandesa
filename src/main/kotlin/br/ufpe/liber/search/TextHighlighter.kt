package br.ufpe.liber.search

import br.ufpe.liber.EagerInProduction
import gg.jte.Content
import gg.jte.html.HtmlContent
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.Fields
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query
import org.apache.lucene.search.highlight.Fragmenter
import org.apache.lucene.search.highlight.Highlighter
import org.apache.lucene.search.highlight.NullFragmenter
import org.apache.lucene.search.highlight.QueryScorer
import org.apache.lucene.search.highlight.SimpleHTMLFormatter
import org.apache.lucene.search.highlight.SimpleSpanFragmenter
import org.apache.lucene.search.highlight.TokenSources

@Singleton
@EagerInProduction
class TextHighlighter(private val analyzer: Analyzer) {
    @Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
    companion object {
        const val MAX_NUM_FRAGMENTS = 4 // better for top results
        lateinit var staticAnalyzer: Analyzer // skipcq: KT-W1047

        @Suppress("detekt:ReturnCount")
        fun highlightText(query: String, text: String): String {
            if (query.isBlank()) return text

            val highlighter = TextHighlighter(staticAnalyzer)
            val parsedQuery = highlighter.query(query)

            if (parsedQuery.toString().isBlank()) return text

            return highlighter.highlightText(
                highlighter.highlighter(
                    query = parsedQuery,
                    fragmenter = NullFragmenter(),
                ),
                text,
                null,
            )
        }
    }

    init {
        staticAnalyzer = analyzer
    }

    private fun query(query: String): Query {
        val queryParser = QueryParser(PageMetadata.TEXT, analyzer)
        return queryParser.parse(query)
    }

    fun highlighter(
        query: Query,
        scorer: QueryScorer = QueryScorer(query, PageMetadata.TEXT),
        fragmenter: Fragmenter = SimpleSpanFragmenter(scorer),
    ): Highlighter {
        val formatter = SimpleHTMLFormatter("<mark>", "</mark>")
        val highlighter = Highlighter(formatter, scorer).apply {
            textFragmenter = fragmenter
        }

        return highlighter
    }

    private fun highlightText(highlighter: Highlighter, content: String, fields: Fields?): String {
        val tokenStream = TokenSources.getTokenStream(
            PageMetadata.TEXT,
            fields,
            content,
            analyzer,
            highlighter.maxDocCharsToAnalyze - 1,
        )
        return highlighter.getBestFragments(tokenStream, content, MAX_NUM_FRAGMENTS).joinToString(" ... ")
    }

    fun highlightContent(highlighter: Highlighter, content: String, fields: Fields?): Content =
        HtmlContent { it.writeContent(highlightText(highlighter, content, fields)) }
}
