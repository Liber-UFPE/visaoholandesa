package br.ufpe.liber.search

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
class TextHighlighter(private val analyzer: Analyzer, private val contentSanitizer: ContentSanitizer) {
    private fun query(query: String): Query = QueryParser(PageMetadata.TEXT, analyzer).parse(query)

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

        return contentSanitizer.sanitize(highlighter.getBestFragment(tokenStream, content))
    }

    fun highlightContent(highlighter: Highlighter, content: String, fields: Fields?): Content =
        HtmlContent { it.writeContent(highlightText(highlighter, content, fields)) }

    fun highlightText(query: String, text: String): String =
        highlightText(highlighter(query = query(query), fragmenter = NullFragmenter()), text, null)
}
