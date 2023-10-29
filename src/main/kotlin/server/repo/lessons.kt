package server.repo

import server.model.Lesson

val lessonsRepo = ListRepo<Lesson>()

val basicLessonsData = listOf(
    Lesson("Математика", "КСР", 140),
    Lesson("Физка", "Лаба", 130),
    Lesson("История", "Лекция", 90),
    Lesson("Биология", "Лаба", 100)
)

//val studentSets = listOf(
//    studentsRepoTestData.filter { it.group == "29m" }.map { it.fullID }.toSet(),
//    studentsRepoTestData.filter { it.group == "29z" }.map { it.fullID }.toSet(),
//    listOf(11, 10, 9, 8).map { studentsRepoTestData[it] }.map { it.fullID }.toSet(),
   // listOf(0, 1, 11).map { studentsRepoTestData[it] }.map { it.fullID }.toSet()
//)

val lessonsRepoTestData = basicLessonsData.mapIndexed { i, l ->
    Lesson(
        l.name,
        l.type,
        l.totalHours,
        setOf(teachersRepoTestData[i].shortID),//(добавить препода(ошибка из-за разницы массивов 4 предмета 3 учителя))
        //studentSets[i]
    )
}