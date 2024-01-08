package br.ufpe.liber.model

import io.micronaut.context.annotation.Bean
import io.micronaut.core.async.publisher.AsyncSingleResultPublisher
import io.micronaut.core.io.ResourceResolver
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.scheduling.TaskExecutors
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.NavigableMap
import java.util.Optional
import java.util.TreeMap
import java.util.concurrent.ExecutorService

@OptIn(ExperimentalSerializationApi::class)
@Singleton
class BookRepository(resourceResolver: ResourceResolver) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BookRepository::class.java)
    }

    private val books: NavigableMap<Long, Book> = TreeMap()

    init {
        @Suppress("detekt:MagicNumber")
        for (bookId in 1..14) {
            resourceResolver.getResource("classpath:data/json/book-$bookId.json").ifPresent { url ->
                val book: Book = Json.decodeFromStream(url.openStream())
                logger.info("Book $bookId fully loaded from data/json/book-$bookId.json file")
                books[book.id] = book
            }
        }
    }

    fun listAll(): List<Book> = books.values.toList()
    fun hasBooks(): Boolean = books.isNotEmpty()
    fun get(id: Long): Optional<Book> = Optional.ofNullable(books[id])
}

@Bean
class BookRepositoryHealthIndicator(
    private val bookRepository: BookRepository,
    @Named(TaskExecutors.BLOCKING) private val executorService: ExecutorService,
) : HealthIndicator {
    companion object {
        val UP = HealthStatus(HealthStatus.NAME_UP, "Books Repository is operational", true, null)
        val DOWN = HealthStatus(HealthStatus.NAME_DOWN, "Books Repository is NOT operational", false, null)
    }

    override fun getResult(): Publisher<HealthResult> = AsyncSingleResultPublisher(executorService, ::getHealthResult)

    private fun getHealthResult(): HealthResult {
        val builder = HealthResult.builder("repository")
        return if (bookRepository.hasBooks()) {
            builder.status(UP).build()
        } else {
            builder.status(DOWN).build()
        }
    }
}
