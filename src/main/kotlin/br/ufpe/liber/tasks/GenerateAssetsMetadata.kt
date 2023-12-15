package br.ufpe.liber.tasks

import br.ufpe.liber.assets.Asset
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

    private fun detectMediaType(file: File): MediaType {
        val metadata = Metadata()
        return tika.detector.detect(TikaInputStream.get(file.toPath(), metadata), metadata)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val assetsParentDir = File(args.first())
        val metafile = File(assetsParentDir, "assets-metadata.json")

        val metadata: MutableMap<String, Asset> = mutableMapOf()
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

                    val filename = matcher.group("filename")
                    val hash = matcher.group("hash")
                    val extension = matcher.group("extension")

                    val original = "$filename.$extension"

                    metadata[original] = Asset(filename, hash, integrity, extension, mediaType.toString())
                }
            }

        val prettyJson = Json {
            prettyPrint = true
        }

        metafile.writeText(prettyJson.encodeToString(metadata))
    }
}
