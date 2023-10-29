package server.model

import kotlinx.serialization.*

@Serializable
class Teacher(val firstname: String, val surname: String) {
    val shortID: String
        get() = "$firstname $surname"
    val detailedData: String
        get() = "$firstname $surname"
}