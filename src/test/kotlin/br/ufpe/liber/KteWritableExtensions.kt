package br.ufpe.liber

import br.ufpe.liber.controllers.KteWriteable
import io.kotest.matchers.should
import io.kotest.matchers.string.contain
import java.io.StringWriter

fun KteWriteable.asString(): String {
    val writer = StringWriter()
    this.writeTo(writer)
    return writer.toString()
}

infix fun KteWriteable.shouldContain(substr: String): KteWriteable {
    this.asString() should contain(substr)
    return this
}
