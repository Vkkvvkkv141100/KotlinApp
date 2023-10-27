package ru.myproject.edu.server.repo

import ru.altmanea.edu.server.model.Config
import ru.altmanea.edu.server.model.Group
import java.util.*

val groupsRepo = ListRepo<Group>()


val groupsRepoTestData = listOf(
    Group("29m"),
    Group("28i"),
    Group("29z"),
    Group("28z"),
)

