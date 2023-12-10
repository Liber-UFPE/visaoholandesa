package br.ufpe.liber.views

import org.owasp.encoder.Encode

object JavaScriptHelpers {
    fun jsonLdEncode(value: String?): String = Encode
        .forJavaScriptBlock(value) // safe
        .replace("\\-", "-") // json-ld compliant for the sake of this project only
}
