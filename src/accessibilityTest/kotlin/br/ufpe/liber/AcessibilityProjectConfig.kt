package br.ufpe.liber

import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension

object AcessibilityProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)
}
