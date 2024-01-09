package br.ufpe.liber.search

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.br.BrazilianAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.PhraseQuery
import org.apache.lucene.search.TermQuery

data class QueryComposer(
    val allWords: String = "",
    val oneOfWords: String = "",
    val exactPhrase: String = "",
    val notWords: String = "",
) {
    private val analyzer: Analyzer = StandardAnalyzer(BrazilianAnalyzer.getDefaultStopSet())
    val query: String = buildQuery()

    fun isEmpty(): Boolean = query.isBlank()

    private fun buildQuery(): String {
        val builder = BooleanQuery.Builder()
        processTokenStream(allWords, builder, BooleanClause.Occur.MUST)
        processTokenStream(oneOfWords, builder, BooleanClause.Occur.SHOULD)
        processExactPhraseTokenStream(exactPhrase, builder)
        processTokenStream(notWords, builder, BooleanClause.Occur.MUST_NOT)

        return builder.build().toString()
    }

    /**
     * See "Using the TokenStream API" in Lucene docs:
     * https://lucene.apache.org/core/9_8_0/core/org/apache/lucene/analysis/package-summary.html?is-external=true
     */
    private fun processTokenStream(text: String, builder: BooleanQuery.Builder, occur: BooleanClause.Occur) {
        if (text.isNotBlank()) {
            val tokenStream = analyzer.tokenStream("", text)
            val charAttribute: CharTermAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)

            tokenStream.use { ts ->
                ts.reset()
                while (ts.incrementToken()) {
                    val termQuery = TermQuery(Term("", charAttribute.toString()))
                    builder.add(BooleanClause(termQuery, occur))
                }
                ts.end()
            }
        }
    }

    private fun processExactPhraseTokenStream(text: String, builder: BooleanQuery.Builder) {
        if (text.isBlank()) {
            return
        }
        val tokenStream = analyzer.tokenStream("", text)
        val charAttribute: CharTermAttribute = tokenStream.addAttribute(CharTermAttribute::class.java)
        val phraseQueryBuilder = PhraseQuery.Builder()

        tokenStream.use { ts ->
            ts.reset()
            while (ts.incrementToken()) {
                phraseQueryBuilder.add(Term("", charAttribute.toString()))
            }
            builder.add(phraseQueryBuilder.build(), BooleanClause.Occur.SHOULD)
            ts.end()
        }
    }

    companion object {
        fun empty() = QueryComposer()
    }
}
