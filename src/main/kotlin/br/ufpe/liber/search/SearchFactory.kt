package br.ufpe.liber.search

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.br.BrazilianAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.store.Directory

@Factory
class SearchFactory {
    @Singleton
    fun createDirectory(): Directory = ByteBuffersDirectory()

    @Singleton
    fun createIndexSearcher(directory: Directory): IndexSearcher = IndexSearcher(DirectoryReader.open(directory))

    @Singleton
    fun createAnalyzer(): Analyzer {
        val stopWords = CharArraySet(BrazilianAnalyzer.getDefaultStopSet(), true)
        listOf(
            "a",
            "ate",
            "até",
            "e",
            "foi",
            "nao",
            "no",
            "nossa",
            "nossas",
            "nosso",
            "nossos",
            // "nota" is used extensively as a markup word
            "nota",
            "não",
            "para",
            "pela",
            "sao",
            "são",
            "tambem",
            "também",
            "tem",
            "à",
            "é",
        ).forEach(stopWords::add)
        return BrazilianAnalyzer(stopWords)
    }
}
