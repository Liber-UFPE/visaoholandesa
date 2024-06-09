package br.ufpe.liber.assets

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import java.time.Duration

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

class CacheControlTest :
    BehaviorSpec({
        given("CacheControl") {
            `when`(".toString") {
                forAll(
                    table(
                        headers("Cache Control Configuration", "Expected header value"),
                        row(CacheControl(Duration.ofSeconds(10)), "max-age=10"),
                        row(CacheControl(Duration.ofSeconds(10), noCache = true), "max-age=10, no-cache"),
                        row(CacheControl(Duration.ofSeconds(10), noStore = true), "max-age=10, no-store"),
                        row(CacheControl(Duration.ofSeconds(10), noTransform = true), "max-age=10, no-transform"),
                        row(CacheControl(Duration.ofSeconds(10), mustRevalidate = true), "max-age=10, must-revalidate"),
                        row(
                            CacheControl(Duration.ofSeconds(10), proxyRevalidate = true),
                            "max-age=10, proxy-revalidate",
                        ),
                        row(CacheControl(Duration.ofSeconds(10), mustUnderstand = true), "max-age=10, must-understand"),
                        row(CacheControl(Duration.ofSeconds(10), private = true), "max-age=10, private"),
                        row(CacheControl(Duration.ofSeconds(10), public = true), "max-age=10, public"),
                        row(CacheControl(Duration.ofSeconds(10), immutable = true), "max-age=10, immutable"),
                        row(
                            CacheControl(Duration.ofSeconds(10), staleWhileRevalidate = true),
                            "max-age=10, stale-while-revalidate",
                        ),
                        row(
                            CacheControl(Duration.ofSeconds(10), public = true, immutable = true),
                            "max-age=10, public, immutable",
                        ),
                    ),
                ) { configuration, expectedHeader ->
                    then("generate header $expectedHeader") {
                        configuration.toString() shouldBe expectedHeader
                    }
                }
            }
        }
    })
