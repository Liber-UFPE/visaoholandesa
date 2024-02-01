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
        val resourceResolver: ResourceResolver = mockk()
        every { resourceResolver.getResource("classpath:public/assets-metadata.json") } answers {
            Optional.of(File("src/test/resources/public/test-assets-metadata.json").toURI().toURL())
        }

        val assetsResolver = AssetsResolver(resourceResolver)

        `when`("#at") {
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

        `when`("#fromHashed") {
            then("should find using hashed name") {
                forAll(
                    row(
                        "/javascripts/main.34UGRNNI.js",
                        "/javascripts/main",
                        "34UGRNNI",
                        "js",
                        "application/javascript",
                    ),
                    row("/stylesheets/main.Y6PST7YS.css", "/stylesheets/main", "Y6PST7YS", "css", "text/css"),
                ) { requested, original, expected, extension, mediaType ->
                    assetsResolver.fromHashed(requested) shouldBePresent { result ->
                        result.basename shouldBe original
                        result.hash shouldBe expected
                        result.extension shouldBe extension
                        result.mediaType shouldBe mediaType
                    }
                }
            }

            then("should return empty when hashed filename does not exists") {
                assetsResolver.at("/javascripts/not-there.Y6PST7YS.js") shouldBe Optional.empty()
            }
        }
    }
})
