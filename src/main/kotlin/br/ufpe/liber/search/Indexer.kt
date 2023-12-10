package br.ufpe.liber.search

import br.ufpe.liber.EagerInProduction
import br.ufpe.liber.model.BookRepository
import jakarta.inject.Singleton
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.FieldType
import org.apache.lucene.document.StoredField
import org.apache.lucene.index.IndexOptions
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
@EagerInProduction
class Indexer(
    private val bookRepository: BookRepository,
    private val directory: Directory,
    private val analyzer: Analyzer,
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Indexer::class.java)
    }

    // This is optimized for fields that need highlighting
    private fun richTextField(name: String, content: String): Field {
        val fieldType = FieldType()
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS)
        fieldType.setStored(true)
        fieldType.setTokenized(true)
        fieldType.setOmitNorms(false)

        fieldType.setStoreTermVectors(true) // captures frequencies
        fieldType.setStoreTermVectorPositions(true) // depends on freqs
        fieldType.setStoreTermVectorOffsets(true) // depends on freqs

        return Field(name, content, fieldType)
    }

    private fun safeAddField(name: String, value: String?, document: Document) {
        if (value != null) {
            document.add(StoredField(name, value))
        }
    }

    init {
        logger.info("Starting to index all books")

        IndexWriter(directory, IndexWriterConfig(analyzer)).use { writer ->
            bookRepository.listAll().forEach { book ->
                logger.info("Indexing book with id ${book.id}")
                book.pages.forEach { page ->
                    val doc = Document()

                    // Book nullable fields
                    safeAddField(BookMetadata.ALTERNATIVE, book.alternative, doc)
                    safeAddField(BookMetadata.DATE, book.date, doc)
                    safeAddField(BookMetadata.SUBJECT, book.subject, doc)
                    safeAddField(BookMetadata.DESCRIPTION, book.description, doc)
                    safeAddField(BookMetadata.RIGHTS, book.rights, doc)
                    safeAddField(BookMetadata.SOURCE, book.source, doc)

                    // Book fields
                    doc.add(StoredField(BookMetadata.ID, book.id))
                    doc.add(StoredField(BookMetadata.TITLE, book.title))
                    doc.add(StoredField(BookMetadata.CREATOR, book.creator))
                    doc.add(StoredField(BookMetadata.PUBLISHER, book.publisher))
                    doc.add(StoredField(BookMetadata.LOCAL, book.local))
                    doc.add(StoredField(BookMetadata.COLLECTION, book.collection))
                    doc.add(StoredField(BookMetadata.LANGUAGE, book.language))
                    doc.add(StoredField(BookMetadata.CONTRIBUTOR, book.contributor))
                    doc.add(StoredField(BookMetadata.TEXT, book.text))

                    // Page fields
                    doc.add(StoredField(PageMetadata.ID, page.id))
                    doc.add(StoredField(PageMetadata.NUMBER, page.number))
                    doc.add(richTextField(PageMetadata.TEXT, page.text))

                    writer.addDocument(doc)
                }
            }
        }
    }
}
