package br.ufpe.liber

import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient

// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

fun BlockingHttpClient.get(path: String): HttpResponse<String> = this.exchange(
    path,
    String::class.java,
    String::class.java,
)
