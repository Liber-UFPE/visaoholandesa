package br.ufpe.liber.controllers

import br.ufpe.liber.search.Indexer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.http.uri.UriBuilder
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class SearchControllerTest(
    private val server: EmbeddedServer,
    private val context: ApplicationContext,
) : BehaviorSpec({
    val client = context
        .createBean(
            HttpClient::class.java,
            server.url,
            DefaultHttpClientConfiguration().apply { isExceptionOnErrorStatus = false },
        )
        .toBlocking()

    beforeSpec {
        // Indexer is now a @Singleton (lazy-loading), therefore we need to
        // force its initialization to force index creation.
        context.createBean(Indexer::class.java)
    }

    given("SearchController") {
        `when`("GET /search") {
            then("should return zero results if query is not present") {
                val url = UriBuilder.of(server.uri).path("/search").build()
                val request = HttpRequest.GET<Unit>(url)
                client.retrieve(request) shouldContain "Ooops, nenhum resultado foi encontrado para a busca por"
            }

            then("should return zero results if query is blank") {
                val url = UriBuilder.of(server.uri).path("/search").queryParam("query", "").build()
                val request = HttpRequest.GET<Unit>(url)
                client.retrieve(request) shouldContain "Ooops, nenhum resultado foi encontrado para a busca por"
            }

            then("should return zero results if query does not match any document") {
                val wontMatchQuery = "a2s3d4f5g6h7j8k9l0"
                val url = UriBuilder.of(server.uri).path("/search").queryParam("query", wontMatchQuery).build()
                val request = HttpRequest.GET<Unit>(url)
                client.retrieve(request) shouldContain "Ooops, nenhum resultado foi encontrado para a busca por"
            }

            then("highlight matches in the search results page") {
                val query = "recife"
                val url = UriBuilder.of(server.uri).path("/search").queryParam("query", query).build()
                val request = HttpRequest.GET<Unit>(url)
                client.retrieve(request) shouldContain "resultados para a busca por <mark>recife</mark>"
            }

            then("return empty results if query is invalid") {
                val invalidLuceneQuery = "+-invalid+-"
                val url = UriBuilder.of(server.uri)
                    .path("/search")
                    .queryParam("query", invalidLuceneQuery)
                    .build()
                val request = HttpRequest.GET<Unit>(url)
                val body = client.retrieve(request)
                body shouldContain "Ooops, nenhum resultado foi encontrado para a busca por"
            }
        }

        `when`("GET /advanced-search") {
            then("show form when parameters are not present") {
                val url = UriBuilder.of(server.uri).path("/advanced-search").build()
                val request = HttpRequest.GET<Unit>(url)
                client.retrieve(request) shouldContain
                    "A busca avançada combina os campos do formulário para gerar um resultado mais preciso"
            }

            then("show form when parameters are all empty") {
                val url = UriBuilder.of(server.uri)
                    .path("/advanced-search")
                    .queryParam("allWords", "")
                    .queryParam("oneOfWords", "")
                    .queryParam("exactPhrase", "")
                    .queryParam("notWords", "")
                    .build()
                val request = HttpRequest.GET<Unit>(url)
                client.retrieve(request) shouldContain
                    "A busca avançada combina os campos do formulário para gerar um resultado mais preciso"
            }

            then("run the query when parameters are present") {
                forAll(
                    // allWords, oneOfWords, exactPhrase, notWords
                    // Only one of the parameters
                    row("recife", "", "", ""),
                    row("", "recife", "", ""),
                    row("", "", "recife", ""),
                    // Multiple parameters mixed
                    row("recife", "olinda", "", ""),
                    row("recife", "", "Rio Salgado", ""),
                    row("recife", "olinda", "Rio Salgado", "Mauritstad"),
                ) { allWords, oneOfWords, exactPhrase, notWords ->
                    val url = UriBuilder.of(server.uri)
                        .path("/advanced-search")
                        .queryParam("allWords", allWords)
                        .queryParam("oneOfWords", oneOfWords)
                        .queryParam("exactPhrase", exactPhrase)
                        .queryParam("notWords", notWords)
                        .build()
                    val request = HttpRequest.GET<Unit>(url)
                    client.retrieve(request) shouldContain "resultados para a busca por"
                }
            }
        }
    }
})
