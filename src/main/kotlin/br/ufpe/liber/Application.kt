package br.ufpe.liber

import io.micronaut.context.env.Environment
import io.micronaut.kotlin.runtime.startApplication
import org.slf4j.LoggerFactory
import java.util.Properties

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

object Application {
    @JvmStatic
    @Suppress("detekt:SpreadOperator")
    fun main(args: Array<String>) {
        startApplication<Application>(*args) {
            val logger = LoggerFactory.getLogger(Application.javaClass)
            // Fallback to dev environment if none is specified.
            environments(System.getenv().getOrDefault("MICRONAUT_ENVIRONMENTS", Environment.DEVELOPMENT))
            eagerInitAnnotated(EagerInProduction::class.java)
            resourceLoader.getResource("META-INF/MANIFEST.MF").ifPresent { url ->
                val properties = Properties()
                url.openStream().use { inputStream -> properties.load(inputStream) }
                for ((name, value) in properties) {
                    logger.info("Manifest property $name: $value")
                }
            }
        }
    }
}
