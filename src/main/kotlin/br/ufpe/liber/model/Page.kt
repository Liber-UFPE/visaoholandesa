package br.ufpe.liber.model

import kotlinx.serialization.Serializable

@Serializable
data class Page(val id: Long, val text: String, val number: Long)
