package br.ufpe.liber.controllers

import br.ufpe.liber.assets.AssetsResolver
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.micronaut.core.io.ResourceResolver
import io.micronaut.http.HttpHeaders.CACHE_CONTROL
import io.micronaut.http.HttpHeaders.CONTENT_ENCODING
import io.micronaut.http.HttpHeaders.ETAG
import io.micronaut.http.HttpHeaders.LAST_MODIFIED
import io.micronaut.http.HttpStatus
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.util.Optional

class AssetsControllerTest : BehaviorSpec({
    given("#asset") {
        val resourceResolver: ResourceResolver = mockk()
        every { resourceResolver.getResource("classpath:public/assets-metadata.json") } answers {
            Optional.of(File("src/test/resources/public/test-assets-metadata.json").toURI().toURL())
        }

        // mock assets and its encoded versions (br, gzip, etc.)
        listOf("javascripts/main.34UGRNNI.js", "stylesheets/main.Y6PST7YS.css").flatMap {
            listOf("public/$it", "public/$it.br", "public/$it.gz", "public/$it.zz")
        }.forEach { mockedAsset ->
            every { resourceResolver.getResourceAsStream("classpath:$mockedAsset") } answers {
                Optional.of("some content".byteInputStream())
            }
        }

        val assetsResolver = AssetsResolver(resourceResolver)
        val assetsController = AssetsController(assetsResolver, resourceResolver)

        then("return HTTP Not Found if asset does not exist") {
            assetsController.asset("asset/not-there.js", "br, gzip").status() shouldBe HttpStatus.NOT_FOUND
        }

        then("return HTTP Not Modified when Etag matches") {
            assetsController.asset(
                "javascripts/main.34UGRNNI.js",
                "br, gzip",
                Optional.of("MzRVR1JOTkkK"),
            ).status() shouldBe HttpStatus.NOT_MODIFIED
        }

        then("return HTTP OK when Etag does NOT matches") {
            assetsController.asset(
                "javascripts/main.34UGRNNI.js",
                "br, gzip",
                Optional.of("not-a-matching-etag"),
            ).status() shouldBe HttpStatus.OK
        }

        forAll(
            row("javascripts/main.34UGRNNI.js", "br, gzip, deflate", HttpStatus.OK, Optional.of("br")),
            row("javascripts/main.34UGRNNI.js", "gzip, deflate", HttpStatus.OK, Optional.of("gzip")),
            row("javascripts/main.34UGRNNI.js", "deflate", HttpStatus.OK, Optional.of("deflate")),
            row("javascripts/main.34UGRNNI.js", "", HttpStatus.OK, Optional.empty<String>()),
            row("stylesheets/main.Y6PST7YS.css", "br, gzip, deflate", HttpStatus.OK, Optional.of("br")),
        ) { requested, acceptEncoding, expectedStatus, expectedEncoding ->
            `when`("requesting $requested with Accept-Encoding $acceptEncoding") {
                then("should return HTTP $expectedStatus") {
                    assetsController.asset(requested, acceptEncoding).status() shouldBe expectedStatus
                }

                then("should return the encoded version $expectedEncoding") {
                    val response = assetsController.asset(requested, acceptEncoding)
                    Optional.ofNullable(response.header(CONTENT_ENCODING)) shouldBe expectedEncoding
                }

                then("should set Cache-Control header") {
                    val response = assetsController.asset(requested, acceptEncoding)
                    response.header(CACHE_CONTROL) shouldStartWith "max-age=31536000, public, immutable"
                }

                then("should set Last-Modified header") {
                    val response = assetsController.asset(requested, acceptEncoding)
                    response.header(LAST_MODIFIED) shouldBe "Thu, 25 Jan 2024 16:46:37 GMT"
                }

                then("should set Etag header") {
                    val response = assetsController.asset(requested, acceptEncoding)
                    response.header(ETAG) shouldBeIn arrayOf("MzRVR1JOTkkK", "WTZQU1Q3WVMK")
                }
            }
        }
    }
})
