package ru.myproject.edu.server.repo

import ru.altmanea.edu.server.model.Config
import ru.altmanea.edu.server.model.Flow
import ru.altmanea.edu.server.model.Group
import java.util.*

val flowsRepo = ListRepo<Flow>()

val flowsRepoTestData = listOf(
    Flow(name = "TestLecture",  "Lecture"),
    Flow(name = "TestPractice",  "Practice"),
    Flow(name = "TestLab",  "Lab")
)