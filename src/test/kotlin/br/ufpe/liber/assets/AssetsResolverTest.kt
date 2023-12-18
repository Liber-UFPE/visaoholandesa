package br.ufpe.liber.assets

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.micronaut.core.io.ResourceResolver
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.util.Optional

class AssetsResolverTest : BehaviorSpec({
    given("AssetsResolver") {
        `when`("#at") {
            val resourceResolver: ResourceResolver = mockk()
            every { resourceResolver.getResourceAsStream("classpath:public/assets-metadata.json") } answers {
                Optional.of(File("src/test/resources/public/assets-metadata.json").inputStream())
            }

            val assetsResolver = AssetsResolver(resourceResolver)

            then("should return hashed version of asset") {
                forAll(
                    row("/javascripts/main.js", "/javascripts/main", "34UGRNNI", "js", "application/javascript"),
                    row("/stylesheets/main.css", "/stylesheets/main", "Y6PST7YS", "css", "text/css"),
                ) { requested, original, expected, extension, mediaType ->
                    assetsResolver.at(requested) shouldBePresent { result ->
                        result.basename shouldBe original
                        result.hash shouldBe expected
                        result.extension shouldBe extension
                        result.mediaType shouldBe mediaType
                    }
                }
            }

            then("should return empty when there is not hashed version") {
                assetsResolver.at("/javascripts/not-there.js") shouldBe Optional.empty()
            }
        }
    }
})
