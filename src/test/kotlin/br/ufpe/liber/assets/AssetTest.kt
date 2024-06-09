package br.ufpe.liber.assets

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.micronaut.http.MediaType
import java.time.Instant
import java.util.Optional

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

class AssetTest :
    BehaviorSpec({
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
                etag = "SzY4RkpENzUK",
                lastModified = Instant.now().toEpochMilli(),
                extension = "js",
                mediaType = "text/javascript",
                supportedEncodings = listOf(brotli, gzip, deflate),
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
                    // gzip has a higher quality value
                    row("gzip;q=1, deflate;q=0.5, br;q=0.1", Optional.of(gzip)),
                    // all zeroes
                    row("gzip;q=0, deflate;q=0, br;q=0", Optional.empty()),
                ) { acceptEncoding, expectedResource ->
                    then("should handle $acceptEncoding header value") {
                        asset.preferredEncodedResource(acceptEncoding) shouldBe expectedResource
                    }
                }
            }
        }

        given("AcceptEncoding") {
            `when`(".compareTo") {
                then("higher q values should come first") {
                    val acceptEncodings = listOf(
                        AcceptEncoding("gzip", Optional.of(0.2f)),
                        AcceptEncoding("br", Optional.of(0.4f)),
                        AcceptEncoding("zz", Optional.of(0.1f)),
                    )

                    acceptEncodings.sorted() shouldBe listOf(
                        AcceptEncoding("br", Optional.of(0.4f)),
                        AcceptEncoding("gzip", Optional.of(0.2f)),
                        AcceptEncoding("zz", Optional.of(0.1f)),
                    )
                }
            }

            @Suppress("IDENTIFIER_LENGTH")
            fun ae(name: String, q: Float) = AcceptEncoding(name, Optional.of(q))

            @Suppress("IDENTIFIER_LENGTH")
            fun ae(name: String, q: Optional<Float> = Optional.empty()) = AcceptEncoding(name, q)

            `when`(".parseHeader") {
                forAll(
                    table(
                        headers("Header value", "Expected accepted encodings"),
                        row("gzip", listOf(ae("gzip"))),
                        row("gzip, compress, br", listOf(ae("gzip"), ae("compress"), ae("br"))),
                        row("gzip;q=1.0, compress, br", listOf(ae("gzip", 1f), ae("compress"), ae("br"))),
                        // in the row below, gzip has a lower priority compared with the default (1)
                        row("gzip;q=0.5, br", listOf(ae("br"), ae("gzip", 0.5f))),
                        row("deflate, gzip;q=1.0, *;q=0.5", listOf(ae("deflate"), ae("gzip", 1.0f), ae("*", 0.5f))),
                        row(
                            "deflate;q=0.1, gzip;q=0.5, br;q=1.0",
                            listOf(ae("br", 1f), ae("gzip", 0.5f), ae("deflate", 0.1f)),
                        ),
                        row("compress;q, br;q=0.5, gzip", listOf(ae("compress"), ae("gzip"), ae("br", 0.5f))),
                        // A sender of q value MUST NOT generate more than three digits after the decimal point
                        row("compress;q=1.12321313", listOf(ae("compress"))),
                        // a q value of 0 means "not acceptable"
                        row("gzip;q=0, br", listOf(ae("br"))),
                        row("gzip;q=0.000, br", listOf(ae("br"))),
                    ),
                ) { headerValue, expectedAcceptedEncodings ->
                    then("should parse $headerValue properly") {
                        AcceptEncoding.parseHeader(headerValue).sorted() shouldBe expectedAcceptedEncodings
                    }
                }
            }
        }
    })
