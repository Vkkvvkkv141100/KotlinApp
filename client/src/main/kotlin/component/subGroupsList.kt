package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.SELECT
import kotlinx.html.js.onClickFunction
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
import ru.altmanea.edu.server.model.Config.Companion.groupsPath
import ru.altmanea.edu.server.model.forString
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface GroupProps : Props {
    var name: String
    var subgroups: List<String>
    var Flows: Set<String>
    var FlowsForGroup: Set<String>

    var addSubgroup: (String) -> Unit
    var deleteSubgroup: (Int) -> Unit
    var addFlowInGroup: (String) -> Unit
    var deleteFlow: (String) -> Unit
}


fun fcGroup() = fc("Group") { props: GroupProps ->
    val nameSubgroupRef = useRef<INPUT>()
    val flowsForGroupRef = useRef<SELECT>()

    h3 {
        +props.name
    }
    ol("rectangle") {
        props.subgroups.mapIndexed { index, it ->
            li {
                Link {
                    attrs.to = it
                    +"$it"
                }
                button {
                    +"X"
                    attrs.onClickFunction = {
                        props.deleteSubgroup(index)
                    }
                }
            }
        }
    }
    if (props.subgroups.size < 2) {
        span {
            p("enterText") {
                +"Имя подгруппы:"
                input {
                    ref = nameSubgroupRef
                }
            }
            button {
                +"Add subgroup"
                attrs.onClickFunction = {
                    nameSubgroupRef.current?.value?.let { nameSubgroup ->
                        if (nameSubgroup != "") {
                            props.addSubgroup(nameSubgroup)
                        }
                    }
                }
            }
        }
    } else {
        +"Максимум подгрупп"
    }

    h3 {
        +"Список потоков группы ${props.name}"
    }
    span {
        ol("rectangle") {
            props.Flows.mapIndexed { index, it1 ->
                li {
                    +it1
                    button {
                        +"X"
                        attrs.onClickFunction = {
                            props.deleteFlow(it1)
                        }
                    }
                }
            }
        }
    }
    select("enterTextInput") {
        ref = flowsForGroupRef

        props.FlowsForGroup.map {
            option {
                +"$it"
                attrs.value = "$it"
            }
        }
    }
    button {
        +"Add flow"
        attrs.onClickFunction = {
            val select = flowsForGroupRef.current.unsafeCast<MySelect>()
            val name = select.value
            if (name != "") {
                props.addFlowInGroup(name)
            }
        }
    }
}


fun fcContainerGroup() = fc("QueryGroup") { _: Props ->
    val queryClient = useQueryClient()
    val params = useParams()
    val GroupID = params["groupId"] ?: "Error"

    val query = useQuery<Any, QueryError, String, Any>(
        "subGroups", {
            fetchText(
                url = groupsPath + GroupID,
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    val queryName = useQuery<Any, QueryError, String, Any>(
        "queryName1", {
            fetchText(
                url = "$groupsPath/$GroupID/name",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    val queryFlows = useQuery<Any, QueryError, String, Any>(
        "FlowsInGroup", {
            fetchText(
                url = "$groupsPath/$GroupID/flows",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    val queryFlowsForGroup = useQuery<Any, QueryError, String, Any>(
        "FlowsForGroup", {
            fetchText(
                url = "$groupsPath/$GroupID/group/Select",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    class MutationData(
        val qq: forString
    )

    val addSubGroupMutation = useMutation<Any, Any, MutationData, Any>(
        { newSubgroupClient ->
            axios<String>(jso {
                url = groupsPath + GroupID
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(newSubgroupClient.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("subGroups")
            }
        })


    val deleteSubGroupMutation = useMutation<Any, Any, Any, Any>(
        { i ->
            axios<String>(jso {
                url = "$groupsPath/${GroupID}/delete"
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = i
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("subGroups")
            }
        }
    )

    val addGroupInFlowMutation = useMutation<Any, Any, MutationData, Any>(
        { newGroupInFlow ->
            axios<String>(jso {
                url = "$groupsPath/${GroupID}/group/flowInGroup"
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(newGroupInFlow.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("FlowsForGroup")
                queryClient.invalidateQueries<Any>("FlowsInGroup")
            }
        }
    )

    val deleteGroupInFlowMutation = useMutation<Any, Any, MutationData, Any>(
        { i ->
            axios<String>(jso {
                url = "$groupsPath/${GroupID}/group/deleteFlowInGroup"
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(i.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("FlowsForGroup")
                queryClient.invalidateQueries<Any>("FlowsInGroup")
            }
        }
    )

    if (query.isLoading or queryFlows.isLoading or queryName.isLoading or queryFlowsForGroup.isLoading) div { +"Loading" }
    else if (query.isError or queryFlows.isError or queryName.isError or queryFlowsForGroup.isError) div { +"Error" }
    else {
        val items: List<String> = Json.decodeFromString(query.data ?: "")
        val flows: Set<String> = Json.decodeFromString(queryFlows.data ?: "")
        val flowsForGroup: Set<String> = Json.decodeFromString(queryFlowsForGroup.data ?: "")
        val name: String = queryName.data ?: ""
        child(fcGroup()) {
            attrs.subgroups = items
            attrs.name = name
            attrs.Flows = flows
            attrs.FlowsForGroup = flowsForGroup
            attrs.addSubgroup = { nsg ->
                addSubGroupMutation.mutate(MutationData(forString(nsg)), null)
            }
            attrs.deleteSubgroup = { index ->
                deleteSubGroupMutation.mutate(index, null)
            }
            attrs.addFlowInGroup = { n ->
                addGroupInFlowMutation.mutate(MutationData(forString(n)), null)
            }
            attrs.deleteFlow = { index ->
                deleteGroupInFlowMutation.mutate(MutationData(forString(index)), null)
            }
        }
    }
}
