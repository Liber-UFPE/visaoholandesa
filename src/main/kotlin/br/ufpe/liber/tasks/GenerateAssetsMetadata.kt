package br.ufpe.liber.tasks

import br.ufpe.liber.assets.Asset
import br.ufpe.liber.assets.Encoding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import java.io.File

object GenerateAssetsMetadata {
    private val tika: TikaConfig = TikaConfig()

    @Suppress("detekt:ForbiddenComment")
    private fun detectMediaType(file: File): MediaType {
        val metadata = Metadata()
        // TODO: Check the return type for JavaScript (text/javascript vs application/javascript)
        // See https://issues.apache.org/jira/browse/TIKA-4119
        return tika.detector.detect(TikaInputStream.get(file.toPath(), metadata), metadata)
    }

    private fun findEncodings(file: File): List<Encoding> = file
        .parentFile
        .walk()
        .maxDepth(1)
        .filter { it.name.startsWith(file.name) }
        .flatMap { encodedFile ->
            when (encodedFile.extension) {
                "br" -> listOf(Encoding("br", "br", 0))
                "gz" -> listOf(Encoding("gzip", "gz", 1))
                "zz" -> listOf(Encoding("deflate", "zz", 2))
                else -> emptyList()
            }
        }.toList()

    @JvmStatic
    fun main(args: Array<String>) {
        val assetsParentDir = File(args.first())
        val metafile = File(assetsParentDir, "assets-metadata.json")

        val metadata: MutableList<Asset> = mutableListOf()
        val regex = "(?<filename>[A-Za-z/-]+).(?<hash>[A-Z0-9]{8}).(?<extension>[a-z]+)".toPattern()

        val digest = DigestUtils.getSha384Digest()
        val integrityGenerator = { file: File ->
            "sha384-${Base64.encodeBase64String(digest.digest(file.readBytes()))}"
        }

        val encodings = listOf("br", "gz", "zz")

        assetsParentDir.walk()
            .filter(File::isFile)
            .filter { !encodings.contains(it.extension) }
            .forEach { file ->
                val assetPath = file.absolutePath.removePrefix(assetsParentDir.absolutePath)
                val matcher = regex.matcher(assetPath)

                if (matcher.matches()) {
                    val integrity = integrityGenerator(file)
                    val mediaType = detectMediaType(file)

                    val basename = matcher.group("filename")
                    val hash = matcher.group("hash")
                    val extension = matcher.group("extension")

                    val source = "$basename.$extension"

                    metadata.add(
                        Asset(
                            basename,
                            source,
                            assetPath,
                            hash,
                            integrity,
                            extension,
                            mediaType.toString(),
                            findEncodings(file).sorted(),
                        ),
                    )
                }
            }

        val prettyJson = Json {
            prettyPrint = true
        }

        metafile.writeText(prettyJson.encodeToString(metadata))
    }
}
