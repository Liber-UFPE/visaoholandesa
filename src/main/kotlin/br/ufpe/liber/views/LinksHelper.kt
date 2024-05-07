package br.ufpe.liber.views

import io.micronaut.core.util.StringUtils
import java.util.Optional

object LinksHelper {
    const val UFPE: String = "https://www.ufpe.br/"
    const val HOLANDAEVOCE: String = "https://www.holandaevoce.nl/"
    const val DCI: String = "https://www.ufpe.br/dci"

    object Liber {
        const val SITE: String = "http://www.liber.ufpe.br/"
        const val INSTAGRAM = "https://www.instagram.com/liberufpe/"
        const val FACEBOOK = "https://www.facebook.com/liberufpe"
        const val YOUTUBE = "https://www.youtube.com/@liberufpe"
        const val LINKTREE = "https://linktr.ee/liberufpe"

        fun link(path: String): String = "$SITE${path.trim()}"
    }

    @JvmStatic
    fun baseUrl(): String = Optional.ofNullable(System.getenv("VISAOHOLANDESA_PATH")).orElse("/")

    fun linkTo(path: String): String = StringUtils.prependUri(baseUrl(), path)
}
