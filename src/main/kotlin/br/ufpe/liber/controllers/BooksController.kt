package br.ufpe.liber.controllers

import br.ufpe.liber.Templates
import br.ufpe.liber.model.BookRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.context.ServerRequestContext
import java.util.Optional

@Controller
@Produces(MediaType.TEXT_HTML)
class BooksController(private val bookRepository: BookRepository, private val templates: Templates) : KteController {
    @Get("/obras")
    fun index() = ok(templates.booksIndex(bookRepository.listAll()))

    @Get("/obra/{bookId}")
    fun show(bookId: Long): HttpResponse<KteWriteable> = bookRepository.get(bookId)
        .map { ok(templates.booksShow(it)) }
        .orElse(notFound(templates.notFound(currentRequestPath())))

    @Get("/obra/{bookId}/pagina/{pageId}")
    fun page(bookId: Long, pageId: Long, @QueryValue query: Optional<String>): HttpResponse<KteWriteable> =
        bookRepository
            .get(bookId)
            .flatMap { book ->
                book.page(pageId).map { page ->
                    ok(templates.booksPage(book, page, query))
                }
            }
            .orElse(notFound(templates.notFound(currentRequestPath())))

    private fun currentRequestPath(): String = ServerRequestContext.currentRequest<Any>().map { it.path }.orElse("")
}
