package ru.altmanea.edu.server.model

import kotlinx.serialization.Serializable

@Serializable
data class Flow(
    val name: String,
    val type: String,
    val participants: Set<String> = emptySet()
)