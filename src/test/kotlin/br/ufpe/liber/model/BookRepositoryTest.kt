package br.ufpe.liber.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import java.util.Optional

@MicronautTest
class BookRepositoryTest(private val bookRepository: BookRepository) :
    BehaviorSpec({
        given("BookRepository") {
            `when`("#listAll") {
                then("should return all the books") {
                    val books = bookRepository.listAll()
                    books.size shouldBe 14
                    books shouldBeSortedWith { book1, book2 -> (book1.id - book2.id).toInt() }
                }
            }

            `when`("#hasBooks") {
                then("should return true when there are books") {
                    bookRepository.hasBooks() shouldBe true
                }
            }

            `when`("#get") {
                then("should return book when found") {
                    bookRepository.get(1) shouldBePresent { book ->
                        book.id shouldBe 1L
                        book.title shouldBe "A Igreja Cristã Reformada no Brasil Holandês"
                    }
                }

                then("should return empty when book not found") {
                    bookRepository.get(1_000) shouldBe Optional.empty()
                }
            }
        }
    })
