package br.ufpe.liber.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.micronaut.health.HealthStatus
import io.mockk.every
import io.mockk.mockk
import reactor.test.StepVerifier
import java.util.Optional
import java.util.concurrent.Executors

class BookRepositoryHealthIndicatorTest : BehaviorSpec({
    given("#getResult") {
        forAll(
            row(true, HealthStatus.UP, true, "Books Repository is operational"),
            row(false, HealthStatus.DOWN, false, "Books Repository NOT is operational"),
        ) { hasBooks, expectedStatus, expectedOperational, expectedDescription ->
            `when`("book repository has books: $hasBooks") {
                val bookRepository: BookRepository = mockk()
                every { bookRepository.hasBooks() } answers { hasBooks }

                val executorService = Executors.newSingleThreadExecutor()
                val bookRepositoryHealthIndicator = BookRepositoryHealthIndicator(bookRepository, executorService)

                then("it should be $expectedStatus") {
                    StepVerifier
                        .create(bookRepositoryHealthIndicator.result)
                        .expectNextMatches { hr -> hr.status == expectedStatus }
                }

                then("it should be operational = $expectedOperational") {
                    StepVerifier
                        .create(bookRepositoryHealthIndicator.result)
                        .expectNextMatches { hr -> hr.status.operational == Optional.of(expectedOperational) }
                }

                then("it should have a description") {
                    StepVerifier
                        .create(bookRepositoryHealthIndicator.result)
                        .expectNextMatches { hr ->
                            hr.status.description == Optional.of(expectedDescription)
                        }
                }
            }
        }
    }
})
