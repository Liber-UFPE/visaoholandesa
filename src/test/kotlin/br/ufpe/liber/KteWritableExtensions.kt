package br.ufpe.liber

import br.ufpe.liber.controllers.KteWriteable
import gg.jte.html.HtmlContent
import gg.jte.html.HtmlTemplateOutput
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

fun HtmlContent.asString(): String {
    val output = object : HtmlTemplateOutput {
        private val buffer = StringBuilder()

        override fun writeContent(value: String) {
            buffer.append(value)
        }

        override fun writeContent(value: String, beginIndex: Int, endIndex: Int) {
            buffer.append(value.substring(beginIndex, endIndex))
        }

        override fun setContext(tagName: String?, attributeName: String?) {
            // do nothing
        }

        override fun toString(): String = buffer.toString().trim()
    }
    this.writeTo(output)
    return output.toString()
}
