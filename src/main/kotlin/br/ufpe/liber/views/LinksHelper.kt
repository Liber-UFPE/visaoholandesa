package br.ufpe.liber.views

import java.util.Optional

object LinksHelper {
    const val UFPE: String = "https://ufpe.br/"
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
    fun baseUrl(): String = Optional.ofNullable(System.getenv("PROJECT_STARTER_PATH")).orElse("/")

    fun linkTo(path: String): String {
        val baseUrl = baseUrl()
        if (path.isBlank()) return baseUrl
        return if (baseUrl == "/") {
            if (path.startsWith("/")) path else "/$path"
        } else if (baseUrl.endsWith("/")) {
            if (path.startsWith("/")) "$baseUrl${path.substring(1)}" else "$baseUrl$path"
        } else {
            if (path.startsWith("/")) "$baseUrl$path" else "$baseUrl/$path"
        }
    }
}
