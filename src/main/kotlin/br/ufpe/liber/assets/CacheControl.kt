package br.ufpe.liber.assets

import java.time.Duration

// Based on https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control, but stills incomplete.
data class CacheControl(
    val maxAge: Duration,
    val noCache: Boolean = false,
    val noStore: Boolean = false,
    val noTransform: Boolean = false,
    val mustRevalidate: Boolean = false,
    val proxyRevalidate: Boolean = false,
    val mustUnderstand: Boolean = false,
    val private: Boolean = false,
    val public: Boolean = false,
    val immutable: Boolean = false,
    val staleWhileRevalidate: Boolean = false,
) {
    private val headerValue: String

    init {
        val builder = StringBuilder()
        builder.append("max-age=${maxAge.seconds}")
        if (noCache) builder.append(", no-cache")
        if (noStore) builder.append(", no-store")
        if (noTransform) builder.append(", no-transform")
        if (mustRevalidate) builder.append(", must-revalidate")
        if (proxyRevalidate) builder.append(", proxy-revalidate")
        if (mustUnderstand) builder.append(", must-understand")
        if (private) builder.append(", private")
        if (public) builder.append(", public")
        if (immutable) builder.append(", immutable")
        if (staleWhileRevalidate) builder.append(", stale-while-revalidate")

        headerValue = builder.toString()
    }

    override fun toString(): String = headerValue
}
