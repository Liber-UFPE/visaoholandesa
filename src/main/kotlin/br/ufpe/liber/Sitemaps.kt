package br.ufpe.liber

import br.ufpe.liber.model.BookRepository
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import java.io.File
import java.nio.charset.StandardCharsets

object Sitemaps {
    @JvmStatic
    fun main(args: Array<String>) {
        ApplicationContext.run(Environment.BARE_METAL).use {
            val bookRepository = it.getBean(BookRepository::class.java)
            val templates = it.getBean(Templates::class.java)

            File("src/main/resources/public/sitemap.xml").writeText(
                templates.sitemap(bookRepository.listAll()).render(),
                StandardCharsets.UTF_8,
            )
        }
    }
}
