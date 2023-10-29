package component

import kotlinext.js.jso
import kotlinx.browser.window
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.useRef
import server.model.Config
import server.model.Item
import server.model.Teacher
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface TeacherListProps : Props {
    var teachers: Set<Item<Teacher>>
    var addTeacher: (String, String) -> Unit
   // var addTeacher: (String, String, Int, Int, String) -> Unit
    //firstname, surname, salary, last qual., gov. number/ID
}

fun fcTeacherList() = fc("TeacherList") { p: TeacherListProps ->
    //HTML text boxes (inputs)
    val firstnameRef = useRef<INPUT>()
    val surnameRef = useRef<INPUT>()
//    val salaryRef = useRef<INPUT>()
//    val lastQualRef = useRef<INPUT>()
//    val govNumberRef = useRef<INPUT>()

    div {
        h4 { +"Add teacher: " }
        input { ref = firstnameRef; attrs.placeholder = "Firstname" }
        //placeholder attribute creates pseudo-value/input tip for users
        input { ref = surnameRef; attrs.placeholder = "Surname" }
//        input { ref = salaryRef; attrs.placeholder = "Salary (number)"; attrs.type = InputType.number }
//        //type attribute hard-locks field input type ("number" allows only numbers)
//        input { ref = lastQualRef;attrs.placeholder = "Last re-qualification (year)"; attrs.type = InputType.number }
//        input { ref = govNumberRef; attrs.placeholder = "Government number/ID" }
        button {
            +"+"
            attrs.onClickFunction = {
                firstnameRef.current?.value?.let { fn ->
                    if (fn.isBlank())
                        window.alert("<Add teacher>: 'Firstname' field must not be empty")
                    else
                        surnameRef.current?.value?.let { sn ->
                            if (sn.isBlank())
                                window.alert("<Add teacher>: 'Surname' field must not be empty")
                            else
                                             p.addTeacher(fn, sn)
//                            salaryRef.current?.value?.let { sl ->
//                                lastQualRef.current?.value?.let { lq ->
//                                    if (lq.isBlank())
//                                        window.alert("<Add teacher>: " +
//                                                "'Last re-qualification (year)' field must not be empty")
//                                    else
//                                        govNumberRef.current?.value?.let { gn ->
//                                            if (gn.isBlank())
//                                                window.alert("<Add teacher>: " +
//                                                        "'Government number/ID' field must not be empty")
//                                            else
//                                                p.addTeacher(fn, sn, sl.toInt(), lq.toInt(), gn)
//                                        }
//                                }
//                            }
                        }
                }
            }
        }
    }

    div {
        h4 { +"Преподаватели:" }
        ul {
            p.teachers.map {
                li {
                    Link {
                        attrs.to = "/teachers/${it.uuid}"
                        +"${it.elem.firstname} ${it.elem.surname}"
                    }
                }
            }
        }
    }
}

//see explanation in file "lesson.kt" (package 'component')
@Serializable
class ClientItemTeacher(
    override val elem: Teacher,
    override val uuid: String,
    override val etag: Long
) : Item<Teacher>

fun fcContainerTeacherList() = fc("QueryTeacherList") { _: Props ->
    val queryClient = useQueryClient()
    val queryTeachers = useQuery<String, QueryError, String, String>(
        "teachersList", { fetchText(Config.teachersURL) })

    val addTeacherMutation = useMutation<Any, Any, Any, Any>({ teacher: Teacher ->
        axios<String>(jso {
            url = Config.teachersURL
            method = "Post"
            headers = json("Content-Type" to "application/json")
            data = JSON.stringify(teacher)
        })
    },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("teachersList")
            }
        }
    )

    if (queryTeachers.isLoading)
        div { +"Loading ..." }
    else if (queryTeachers.isError)
        div { +"Query error. Please contact server administrator at: admin@adminmail." }
    else {
        val teachers: List<ClientItemTeacher> = Json.decodeFromString(queryTeachers.data?:"")
        child(fcTeacherList()) {
            attrs.teachers = teachers.toSet()
            attrs.addTeacher = { fn, sn ->
                addTeacherMutation.mutate(Teacher(fn, sn), null)
            }
        }
    }
}
