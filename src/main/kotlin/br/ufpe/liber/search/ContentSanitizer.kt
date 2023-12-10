package br.ufpe.liber.search

import io.micronaut.core.io.ResourceResolver
import jakarta.inject.Singleton
import org.owasp.validator.html.AntiSamy
import org.owasp.validator.html.Policy

@Singleton
class ContentSanitizer(resourceResolver: ResourceResolver) {
    private val antiSamy: AntiSamy

    init {
        val policy = resourceResolver
            .getResourceAsStream("classpath:antisamy-liber.xml")
            .map(Policy::getInstance)
            .orElse(Policy.getInstance())

        antiSamy = AntiSamy(policy)
    }

    fun sanitize(html: String): String = antiSamy.scan(html).cleanHTML
}
