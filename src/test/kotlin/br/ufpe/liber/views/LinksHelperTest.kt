package br.ufpe.liber.views

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class LinksHelperTest : BehaviorSpec({
    given("LinksHelper") {
        `when`(".Liber.link") {
            then("should create a link that is a path of Liber website") {
                LinksHelper.Liber.link("some-path") shouldBe "http://www.liber.ufpe.br/some-path"
            }

            then("should return Liber website URL when path is empty") {
                LinksHelper.Liber.link("") shouldBe "http://www.liber.ufpe.br/"
            }
        }
    }

    given("LinksHelper.linkTo") {
        `when`("""when base url is "/" """) {
            withEnvironment("PROJECT_STARTER_PATH", "/") {
                forAll(
                    row("/some-path", "/some-path"),
                    row("some-path", "/some-path"),
                    row("/some-path/", "/some-path/"),
                    row("some-path/", "/some-path/"),
                    row("/", "/"),
                    row("", "/"),
                ) { path, expectedResult ->
                    then("generate correct link for $path") {
                        LinksHelper.linkTo(path) shouldBeEqual expectedResult
                    }
                }
            }
        }

        `when`("""when base url is "/base" """) {
            withEnvironment("PROJECT_STARTER_PATH", "/base") {
                forAll(
                    table(
                        headers("path", "expectedResult"),
                        row("/some-path", "/base/some-path"),
                        row("some-path", "/base/some-path"),
                        row("/some-path/", "/base/some-path/"),
                        row("some-path/", "/base/some-path/"),
                        row("/", "/base/"),
                        row("", "/base"),
                    ),
                ) { path, expectedResult ->
                    then("generate correct link for $path") {
                        LinksHelper.linkTo(path) shouldBeEqual expectedResult
                    }
                }
            }
        }

        `when`("""when base url is "/base/" """) {
            withEnvironment("PROJECT_STARTER_PATH", "/base/") {
                forAll(
                    row("/some-path", "/base/some-path"),
                    row("some-path", "/base/some-path"),
                    row("/some-path/", "/base/some-path/"),
                    row("some-path/", "/base/some-path/"),
                    row("/", "/base/"),
                    row("", "/base/"),
                ) { path, expectedResult ->
                    then("generate correct link for $path") {
                        LinksHelper.linkTo(path) shouldBeEqual expectedResult
                    }
                }
            }
        }
    }
})
