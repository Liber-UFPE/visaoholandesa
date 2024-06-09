package br.ufpe.liber.search

import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.health.HealthStatus
import io.mockk.every
import io.mockk.mockk
import reactor.test.StepVerifier
import java.lang.RuntimeException
import java.util.Optional
import java.util.concurrent.Executors

class SearchHealthIndicatorTest :
    BehaviorSpec({
        given("#result") {
            `when`("when search is working") {
                val search: Search = mockk()
                every { search.search("recife") } answers { SearchResults.empty() }

                val executorService = Executors.newSingleThreadExecutor()
                val searchHealthIndicator = SearchHealthIndicator(search, executorService)

                then("it should be UP") {
                    StepVerifier
                        .create(searchHealthIndicator.result)
                        .expectNextMatches { hr -> hr.status == HealthStatus.UP }
                }

                then("it should be operational") {
                    StepVerifier
                        .create(searchHealthIndicator.result)
                        .expectNextMatches { hr -> hr.status.operational == Optional.of(true) }
                }

                then("it should have a description") {
                    StepVerifier
                        .create(searchHealthIndicator.result)
                        .expectNextMatches { hr ->
                            hr.status.description == Optional.of("Search is operational")
                        }
                }
            }

            `when`("when search is NOT working") {
                val search: Search = mockk()
                every { search.search("recife") } throws (RuntimeException())

                val executorService = Executors.newSingleThreadExecutor()
                val searchHealthIndicator = SearchHealthIndicator(search, executorService)

                then("it should be DOWN") {
                    StepVerifier
                        .create(searchHealthIndicator.result)
                        .expectNextMatches { hr -> hr.status == HealthStatus.DOWN }
                }

                then("it should be operational") {
                    StepVerifier
                        .create(searchHealthIndicator.result)
                        .expectNextMatches { hr -> hr.status.operational == Optional.of(false) }
                }

                then("it should have a description") {
                    StepVerifier
                        .create(searchHealthIndicator.result)
                        .expectNextMatches { hr ->
                            hr.status.description == Optional.of("Search is NOT operational")
                        }
                }
            }
        }
    })
