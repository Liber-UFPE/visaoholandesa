@file:Suppress("FILE_NAME_MATCH_CLASS")

package br.ufpe.liber

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import io.micronaut.context.annotation.Factory
import io.micronaut.context.env.Environment
import io.micronaut.kotlin.runtime.startApplication
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.nio.file.Paths

object Application {
    @JvmStatic
    @Suppress("detekt:SpreadOperator")
    fun main(args: Array<String>) {
        startApplication<Application>(*args) {
            // Fallback to dev environment if none is specified.
            environments(System.getenv().getOrDefault("MICRONAUT_ENVIRONMENTS", Environment.DEVELOPMENT))
            eagerInitAnnotated(EagerInProduction::class.java)
        }
    }
}

@Factory
internal class TemplatesFactory {
    private val logger = LoggerFactory.getLogger(TemplatesFactory::class.java)

    @Singleton
    fun createTemplate(environment: Environment): Templates {
        return if (devEnvironment(environment)) {
            logger.info("Hot reloading jte templates")
            val codeResolver = DirectoryCodeResolver(Paths.get("src/main/jte"))
            val templateEngine = TemplateEngine.create(codeResolver, ContentType.Html)
            DynamicTemplates(templateEngine)
        } else {
            logger.info("Use pre-compiled templates")
            StaticTemplates()
        }
    }

    private fun devEnvironment(env: Environment): Boolean = env.activeNames.contains(Environment.DEVELOPMENT)
}
