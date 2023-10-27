package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.SELECT
import kotlinx.html.js.onClickFunction
import kotlinx.html.onFocusOut
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.router.useParams
import react.useRef
import react.useState
import ru.altmanea.edu.server.model.Config.Companion.flowsPath
import ru.altmanea.edu.server.model.Config.Companion.groupsPath
import ru.altmanea.edu.server.model.Flow
import ru.altmanea.edu.server.model.Group
import ru.altmanea.edu.server.model.Item
import ru.altmanea.edu.server.model.forString
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface FlowProps : Props {
    //    var typeFlow: String
    var Flow: Item<Flow>
    var allGroups: List<Item<Group>>
    var participants: List<String>
    var groupsForAdded: List<String>
    var subgroupsForAdded: List<String>
    var deleteParticipants: (Int) -> Unit
    var addParticipants: (String) -> Unit
    var updateFlowName: (String) -> Unit
}

interface MySelectFlow {
    val value: String
}

fun fcFlow() = fc("Flow") { props: FlowProps ->
    val selectRef = useRef<SELECT>()
    val selectRef1 = useRef<SELECT>()
    val flowNameRef = useRef<INPUT>()


    val (state, setState) = useState("")

    h3 {
        +props.Flow.elem.name
    }
    ol("rectangle") {
        props.participants.mapIndexed { index, it ->
            li {
                Link {
                    attrs.to = it.toString()
                    +"$it"
                }
                button {
                    +"X"
                    attrs.onClickFunction = {
                        props.deleteParticipants(index)
                    }
                }
            }
        }
    }

    if (props.Flow.elem.type == "Lecture") {
        span {
            p("enterText") {
                +"Имя группы:"
            }
            select("enterTextInput") {
                ref = selectRef

                props.groupsForAdded.map {
                    option {
                        +it
                        attrs.value = it
                    }
                }
            }
            button {
                +"Add participant"
                attrs.onClickFunction = {
                    val select = selectRef.current.unsafeCast<MySelectFlow>()
                    val name = select.value
                    if (name != "") {
                        props.addParticipants(name)
                    }
                }
            }
        }
    } else if (props.Flow.elem.type == "Practice") {
        if (props.participants.size < 1) {
            span {
                p("enterText") {
                    +"Имя группы:"
                }
                select("enterTextInput") {
                    ref = selectRef

                    props.groupsForAdded.map {
                        option {
                            +"$it"
                            attrs.value = "$it"
                        }
                    }
                }
                button {
                    +"Add participant"
                    attrs.onClickFunction = {
                        val select = selectRef.current.unsafeCast<MySelectFlow>()
                        val name = select.value
                        if (name != "") {
                            props.addParticipants(name)
                        }
                    }
                }
            }
        }
    } else if (props.Flow.elem.type == "Lab") {
        if (props.participants.size < 1) {

            select("enterTextInput") {
                ref = selectRef
                props.allGroups.map {
                    option {

                        +it.elem.name
                        attrs.value = it.elem.name
                        attrs.onFocusOut = it.elem.name
                    }
                }
            }

            button {
                +"Select"
                attrs.onClickFunction = {
                    val select = selectRef.current.unsafeCast<MySelect>()
                    val name = select.value
                    setState(name)
                }
            }
            var currentSubgroup: Set<String> = emptySet()
            props.allGroups.forEach {
                if (it.elem.name == state) {
                    currentSubgroup = it.elem.subgroups
                }
            }
            select("enterTextInput") {
                ref = selectRef1

                currentSubgroup.map {
                    option {
                        +it
                        attrs.value = it
                    }
                }
            }
            button {
                +"Add participant"
                attrs.onClickFunction = {
                    val select = selectRef1.current.unsafeCast<MySelectFlow>()
                    val name = select.value
                    if (name != "") {
                        props.addParticipants(name)
                    }
                }
            }
        }

    }

    span {
        p {
            +"Новое имя для потока"
            input {
                ref = flowNameRef
            }
        }
        button {
            +"Update name"
            attrs.onClickFunction = {
                flowNameRef.current?.value?.let { nameGroup ->
                    if (nameGroup != "") {
                        props.updateFlowName(nameGroup)
                    }
                }
            }
        }
    }

}


fun fcContainerFlow() = fc("QueryFlow") { _: Props ->
    val queryClient = useQueryClient()
    val params = useParams()
    val FlowID = params["flowId"] ?: "Error"

    val query = useQuery<Any, QueryError, String, Any>(
        "Flow", {
            fetchText(
                url = flowsPath + FlowID,
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )
    val querySubGroup = useQuery<Any, QueryError, String, Any>(
        "subGroups", {
            fetchText(
                url = "$flowsPath/$FlowID/Select",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )
    val queryGroup = useQuery<Any, QueryError, String, Any>(
        "GroupList1", {
            fetchText(
                url = "$flowsPath/$FlowID/SelectGroup",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )
    val queryAllGroups = useQuery<Any, QueryError, String, Any>(
        "queryAllGroups", {
            fetchText(
                url = groupsPath,
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )
    val queryObject = useQuery<Any, QueryError, String, Any>(
        "queryObject", {
            fetchText(
                url = "$flowsPath/$FlowID/object",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    class MutationData(
        val qq: forString
    )

    val updateNameFlowMutation = useMutation<Any, Any, MutationData, Any>(
        { newNameFlow ->
            axios<String>(jso {
                url = "$flowsPath/${FlowID}/name"
                method = "Put"
                headers = json(
                    "Content-Type" to "application/json"
                )
                data = JSON.stringify(newNameFlow.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("queryObject")
            }
        }
    )


    val addParticipantMutation = useMutation<Any, Any, MutationData, Any>(
        { newPartClient ->
            axios<String>(jso {
                url = flowsPath + FlowID
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(newPartClient.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("subGroups")
                queryClient.invalidateQueries<Any>("Flow")
                queryClient.invalidateQueries<Any>("GroupList1")
            }
        }
    )


    val deleteParticipantMutation = useMutation<Any, Any, Any, Any>(
        { i ->
            axios<String>(jso {
                url = "$flowsPath/${FlowID}/delete"
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = i
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("subGroups")
                queryClient.invalidateQueries<Any>("Flow")
            }
        }
    )


    if (query.isLoading or querySubGroup.isLoading or queryObject.isLoading or queryGroup.isLoading or queryAllGroups.isLoading) div { +"Loading" }
    else if (query.isError or queryObject.isError or queryGroup.isError or querySubGroup.isError or queryAllGroups.isError) div { +"Error" }
    else {
        val items: List<String> = Json.decodeFromString(query.data ?: "")
        val groupMembers: List<String> = Json.decodeFromString(queryGroup.data ?: "")
        val typeFlow: FlowClient = Json.decodeFromString(queryObject.data ?: "")
        val subgroups: List<String> = Json.decodeFromString(querySubGroup.data ?: "")
        val allGroups: List<GroupClient> = Json.decodeFromString(queryAllGroups.data ?: "")
        child(fcFlow()) {
            attrs.subgroupsForAdded = subgroups
            attrs.participants = items
            attrs.groupsForAdded = groupMembers
            attrs.Flow = typeFlow
            attrs.allGroups = allGroups
            attrs.deleteParticipants = { index ->
                deleteParticipantMutation.mutate(index, null)
            }
            attrs.addParticipants = { n ->
                addParticipantMutation.mutate(MutationData(forString(n)), null)
            }
            attrs.updateFlowName = { n ->
                updateNameFlowMutation.mutate(MutationData(forString(n)), null)
            }
        }
    }
}