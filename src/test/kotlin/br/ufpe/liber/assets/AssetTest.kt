package br.ufpe.liber.assets

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.MediaType

class AssetTest : BehaviorSpec({
    given("Asset") {
        val asset = Asset("/javascripts/main", "K68FJD75", "123456", "js", "text/javascript")

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
    }
})
