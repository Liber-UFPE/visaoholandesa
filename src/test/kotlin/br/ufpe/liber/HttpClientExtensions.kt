package br.ufpe.liber

import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient

fun BlockingHttpClient.get(path: String): HttpResponse<String> = this.exchange(
    path,
    String::class.java,
    String::class.java,
)
