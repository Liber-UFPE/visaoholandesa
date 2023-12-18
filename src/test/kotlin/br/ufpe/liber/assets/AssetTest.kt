package br.ufpe.liber.assets

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.micronaut.http.MediaType
import java.util.Optional

class AssetTest : BehaviorSpec({
    given("Asset") {
        val brotli = Encoding("br", "br", 0)
        val gzip = Encoding("gzip", "gz", 1)
        val deflate = Encoding("deflate", "zz", 2)
        val asset = Asset(
            basename = "/javascripts/main",
            source = "/javascripts/main.js",
            filename = "/javascripts/main.K68FJD75.js",
            hash = "K68FJD75",
            integrity = "sha384-qWyHoR/uZ7x+UjVssG6ex4WUplfdMrwZMRmqQDXnn6uwCmlQUJkwhdifK4iY0EnX",
            extension = "js",
            mediaType = "text/javascript",
            encodings = listOf(brotli, gzip, deflate),
        )

        `when`("#mediaType") {
            then("should return an MediaType object") {
                asset.mediaType() shouldBe MediaType("text/javascript")
            }
        }

        `when`("#fullpath") {
            then("should return without prefix") {
                asset.fullpath() shouldBe "javascripts/main.K68FJD75.js"
            }

            then("should return with prefix") {
                asset.fullpath("/static") shouldBe "/static/javascripts/main.K68FJD75.js"
            }
        }

        `when`("#classpath") {
            then("should return without encoding extension") {
                asset.classpath() shouldBe "classpath:public/javascripts/main.K68FJD75.js"
            }

            then("should return with encoding extension") {
                asset.classpath("br") shouldBe "classpath:public/javascripts/main.K68FJD75.js.br"
            }
        }

        `when`("#variant") {
            then("should return with new extension and prefix") {
                asset.variant("ts", "/static") shouldBe "/static/javascripts/main.K68FJD75.ts"
            }

            then("should return with new extension without prefix") {
                asset.variant("ts") shouldBe "javascripts/main.K68FJD75.ts"
            }
        }

        `when`("#preferredEncodedResource") {
            forAll(
                row("gzip, deflate, br", Optional.of(brotli)),
                row("gzip, deflate", Optional.of(gzip)),
                row("deflate", Optional.of(deflate)),
                row("", Optional.empty()),
            ) { acceptEncoding, expectedResource ->
                then("should find the encoded resource") {
                    asset.preferredEncodedResource(acceptEncoding) shouldBe expectedResource
                }
            }
        }
    }
})
