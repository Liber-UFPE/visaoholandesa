package br.ufpe.liber.views

import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import gg.jte.html.HtmlContent

object Markdown {
    private val options = MutableDataSet().set(Parser.EXTENSIONS, listOf<Extension>(FootnoteExtension.create()))
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun toHtml(content: String): HtmlContent {
        val document = parser.parse(content)
        val html = renderer.render(document)
        return HtmlContent { output -> output.writeContent(html) }
    }
}
