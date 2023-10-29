package server.repo

import server.model.Teacher

val teachersRepo = ListRepo<Teacher>()

val teachersRepoTestData = listOf(
    Teacher("Арсений", "Генералов"),
    Teacher("Иван", "Хохлов"),
    Teacher("Данияр", "Батрудинов"),
    Teacher("Данияр", "Батрудинов")
)