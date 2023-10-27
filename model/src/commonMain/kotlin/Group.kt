package ru.altmanea.edu.server.model

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val name: String,
    val subgroups: Set<String> = emptySet()
)