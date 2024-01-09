package br.ufpe.liber.controllers

import br.ufpe.liber.model.BookRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.kotlin.context.createBean
import io.micronaut.kotlin.http.argumentOf
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class BooksControllerTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
    private val bookRepository: BookRepository,
) : BehaviorSpec({
    val client = context.createBean<HttpClient>(
        server.url,
        DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
    ).toBlocking()

    given("BooksController") {
        `when`("accessing the list of books") {
            then("should get HTTP 200") {
                client.exchange<KteWriteable>("/obras").status shouldBe HttpStatus.OK
            }

            then("should list all the books") {
                val responseBody = client.retrieve("/obras")
                bookRepository.listAll().forEach { book ->
                    responseBody shouldContain book.title
                }
            }
        }

        `when`("showing a book") {
            // Testing show for all the books
            val rows = bookRepository.listAll().map { row("/obra/${it.id}", HttpStatus.OK) }.toTypedArray()
            forAll(*rows) { path, expectedStatus ->
                then("$path should return $expectedStatus") {
                    val request: HttpRequest<Unit> = HttpRequest.GET(path)
                    val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                    response.status shouldBe expectedStatus
                }
            }

            then("return HTTP 404 if book does not exist") {
                val request: HttpRequest<Unit> = HttpRequest.GET("/obra/123123")
                val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                response.status shouldBe HttpStatus.NOT_FOUND
            }
        }

        `when`("showing a page") {
            then("return HTTP 200 for existing page") {
                val book = bookRepository.get(1).get()
                val page = book.pages.first()
                val request: HttpRequest<Unit> = HttpRequest.GET("/obra/${book.id}/pagina/${page.id}")
                val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                response.status shouldBe HttpStatus.OK
            }

            then("return HTTP 404 for page that does not exist in the book") {
                val book = bookRepository.get(1).get()
                val page = book.pages.last()
                val request: HttpRequest<Unit> = HttpRequest.GET("/obra/${book.id}/pagina/${page.id + 1}")
                val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                response.status shouldBe HttpStatus.NOT_FOUND
            }

            then("return HTTP 404 when book does not exist") {
                val book = bookRepository.listAll().last()
                val page = book.pages.first()
                val request: HttpRequest<Unit> = HttpRequest.GET("/obra/${book.id + 1}/pagina/${page.id}")
                val response = client.exchange(request, argumentOf<KteWriteable>(), argumentOf<KteWriteable>())
                response.status shouldBe HttpStatus.NOT_FOUND
            }
        }
    }
})
