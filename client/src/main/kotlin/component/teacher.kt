package component

import kotlinext.js.jso
import kotlinx.browser.window
import kotlinx.html.INPUT
//import kotlinx.html.InputType
import kotlinx.html.SELECT
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import react.query.*
import react.router.useParams
import server.model.*
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface TeacherProps : Props {
    var teacher: Item<Teacher>
    var lessons: List<Item<Lesson>>
    var updateTeacher: (String, String) -> Unit
    var addLesson: (String) -> Unit
    var rmLesson: (String) -> Unit
}

fun fcTeacher() = fc("Teacher") { p: TeacherProps ->
    val firstnameRef = useRef<INPUT>()
    val surnameRef = useRef<INPUT>()
//    val salaryRef = useRef<INPUT>()
//    val lastQualRef = useRef<INPUT>()
//    val govNumberRef = useRef<INPUT>()
    val lessonSelectAddRef = useRef<SELECT>()
    val lessonSelectRmRef = useRef<SELECT>()

    val (firstname, setFirstname) = useState(p.teacher.elem.firstname)
    val (surname, setSurname) = useState(p.teacher.elem.surname)
//    val (salary, setSalary) = useState(p.teacher.elem.salary.toString())
//    val (lastQual, setLastQual) = useState(p.teacher.elem.lastQual.toString())
//    val (govNumber, setGovNumber) = useState(p.teacher.elem.govNumber)

    fun changeOnEdit(setter: StateSetter<String>, ref: MutableRefObject<INPUT>) = { _: Event ->
        setter(ref.current?.value ?: "ERROR!")
    }

    div {
        h4 { +"Редактор преподавателя:" }
        p {
            +"Имя: "
            input {
                ref = firstnameRef
                attrs.value = firstname
                attrs.onChangeFunction = changeOnEdit(setFirstname, firstnameRef)
            }
        }
        p {
            +"Фамилия: "
            input {
                ref = surnameRef
                attrs.value = surname
                attrs.onChangeFunction = changeOnEdit(setSurname, surnameRef)
            }
        }
//        p {
//            +"Salary: "
//            input {
//                ref = salaryRef
//                attrs.value = salary
//                attrs.onChangeFunction = changeOnEdit(setSalary, salaryRef)
//                attrs.type = InputType.number
//            }
//        }
//        p {
//            +"Last re-qualification (year): "
//            input {
//                ref = lastQualRef
//                attrs.value = lastQual
//                attrs.onChangeFunction = changeOnEdit(setLastQual, lastQualRef)
//                attrs.type = InputType.number
//            }
//        }
//        p {
//            +"Government number/ID: "
//            input {
//                ref = govNumberRef
//                attrs.value = govNumber
//                attrs.onChangeFunction = changeOnEdit(setGovNumber, govNumberRef)
//            }
        button {
            +"Обновление преподавателя"
            attrs.onClickFunction = {
                firstnameRef.current?.value?.let { fn ->
                    surnameRef.current?.value?.let { sn ->
                        p.updateTeacher(fn, sn)
                    }
                }
            }
        }
    }

//                            salaryRef.current?.value?.let { sl ->
//                                val slToInt = sl.toIntOrNull()
//                                if (slToInt == null)
//                                    window.alert("<Profile editor>: " +
//                                            "'salary' field must be a number!")
//                                else {
//                                    lastQualRef.current?.value?.let { lq ->
//                                        val lqToInt = lq.toIntOrNull()
//                                        if (lqToInt == null)
//                                            window.alert("<Profile editor>: " +
//                                                    "'Last re-qualification (year)' field must be a number!")
//                                        else {
//                                            govNumberRef.current?.value?.let { gn ->
//                                                p.updateTeacher(fn, sn, slToInt, lqToInt, gn)
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
    div {
        p {
            +"Добавить занятие: "
            select {
                ref = lessonSelectAddRef
                p.lessons.filterNot {
                    it.elem.teachers.toString().contains(p.teacher.elem.shortID)
                }.map {
                    option {
                        attrs.value = it.uuid
                        +"${it.elem.name} (${it.elem.type})"
                    }
                }
            }
            button {
                +"+"
                attrs.onClickFunction = {
                    val select = lessonSelectAddRef.current.unsafeCast<SelectedElement>()
                    if (select.value != "" && select.value != " ")
                        p.addLesson(select.value)
                    else
                        window.alert("<Add lesson>: Empty values are not allowed.")
                }
            }
        }
    }
    div {
        p {
            +"Удалить занятие: "
            select {
                ref = lessonSelectRmRef
                p.lessons.filter {
                    it.elem.teachers.toString().contains(p.teacher.elem.shortID)
                }.map {
                    option {
                        attrs.value = it.uuid
                        +"${it.elem.name} (${it.elem.type})"
                    }
                }
            }
            button {
                +"-"
                attrs.onClickFunction = {
                    val select = lessonSelectRmRef.current.unsafeCast<SelectedElement>()
                    if (select.value != "" && select.value != " ")
                        p.rmLesson(select.value)
                    else
                        window.alert("<Remove lesson>: Empty values are not allowed.")
                }
            }
        }
    }
    div {
        h4 { +"Занятия этого преподавателя : " }
        ol {
            val prescribedLessons =
                p.lessons.filter { it.elem.teachers.toString().contains(p.teacher.elem.shortID) }
            if (prescribedLessons.isEmpty())
                li { +"empty" }
            else {
                prescribedLessons.map {
                    li {
                        a {
                            attrs.href = "http://localhost:8000/#/lessons/${it.uuid}/details"
                            +"${it.elem.name} (${it.elem.type})"
                        }
                    }
                }
            }
        }
    }
}

fun fcContainerTeacher() = fc("ContainerTeacher") { _: Props ->
    val queryClient = useQueryClient()
    val teacherParams = useParams()
    val teacherId = teacherParams["id"] ?: "Route param error"

    val queryLessons = useQuery<String, QueryError, String, String>(
        "lessonsList", { fetchText(Config.lessonsURL ) })
    val queryTeacher = useQuery<String, QueryError, String, String>(
        teacherId, { fetchText(Config.teachersURL + teacherId) })

    val updateTeacherMutation = useMutation<Any, Any, Teacher, Any>({ elem ->
        axios<String>(jso {
            url = "${Config.teachersURL}/$teacherId"
            method = "Put"
            headers = json("Content-Type" to "application/json")
            data = Json.encodeToString(elem)
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(teacherId)
            }
        }
    )
    //updating teacher name or surname will cause loss of all prescribed lessons
    //because this query is not meant to update/handle teachers names change

    val addLessonMutation = useMutation<Any, Any, String, Any>({ lessonId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$teacherId/addt"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(teacherId)
                queryClient.invalidateQueries<Any>("lessonsList")
            }
        }
    )

    val rmLessonMutation = useMutation<Any, Any, String, Any>({ lessonId ->
        axios<String>(jso {
            url = "${Config.lessonsURL}/$lessonId/details/$teacherId/rmt"
            method = "Post"
            headers = json("Content-Type" to "application/json")
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>(teacherId)
                queryClient.invalidateQueries<Any>("lessonsList")
            }
        }
    )

    if (queryLessons.isLoading or queryTeacher.isLoading)
        div { +"Loading ..." }
    else if (queryLessons.isError or queryTeacher.isLoading)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    else {
        val lessons: List<ClientItemLesson> = Json.decodeFromString(queryLessons.data?:"")
        val teacher: ClientItemTeacher = Json.decodeFromString(queryTeacher.data?:"")
        child(fcTeacher()) {
            attrs.teacher = teacher
            attrs.lessons = lessons
            attrs.updateTeacher = { fn, sn ->
                updateTeacherMutation.mutate(Teacher(fn, sn), null)
            }
//            attrs.updateTeacher = { fn, sn, sl, lq, gn ->
//                updateTeacherMutation.mutate(Teacher(fn, sn, sl, lq, gn), null)
//            }
            attrs.addLesson = { lessonId ->
                addLessonMutation.mutate(lessonId, null)
            }
            attrs.rmLesson = { lessonId ->
                rmLessonMutation.mutate(lessonId, null)
            }
        }
    }
}
