package br.ufpe.liber

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.nio.file.Paths

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

@Factory
internal class TemplatesFactory {
    private val logger = LoggerFactory.getLogger(TemplatesFactory::class.java)

    @Singleton
    @EagerInProduction
    fun createTemplate(environment: Environment): Templates = if (isDevelopmentEnvironment(environment)) {
        logger.info("Hot reloading jte templates")
        val codeResolver = DirectoryCodeResolver(Paths.get("src/main/jte"))
        val templateEngine = TemplateEngine.create(codeResolver, ContentType.Html)
        DynamicTemplates(templateEngine)
    } else {
        logger.info("Use pre-compiled templates")
        StaticTemplates()
    }

    private fun isDevelopmentEnvironment(env: Environment): Boolean = env.activeNames.contains(Environment.DEVELOPMENT)
}
