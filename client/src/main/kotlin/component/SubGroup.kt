package component

import kotlinext.js.jso

import kotlinext.js.jso
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
import react.router.useParams
import react.useRef
import ru.altmanea.edu.server.model.Config.Companion.groupsPath
import ru.altmanea.edu.server.model.forString

import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface SubgroupProps : Props {
    var Flows: Set<String>
    var name: String
    var deleteFlow: (String) -> Unit
    var addFlowInSubgroup: (String) -> Unit
    var FlowsForSubgroup: Set<String>
}

fun fcSubgroup() = fc("Subgroup") { props: SubgroupProps ->
    val selectRef = useRef<SELECT>()



    h3 { +"Потоки подгруппы" }
    ol("rectangle") {
        props.Flows.map { it1 ->
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
    select("enterTextInput") {
        ref = selectRef

        props.FlowsForSubgroup.map {
            option {
                +"$it"
                attrs.value = "$it"
            }
        }
    }
    button {
        +"Add flow"
        attrs.onClickFunction = {
            val select = selectRef.current.unsafeCast<MySelect>()
            val name = select.value
            if (name != "") {
                props.addFlowInSubgroup(name)
            }
        }
    }

}

fun fcContainerSubgroup() = fc("QuerySubgroup") { _: Props ->
    val queryClient = useQueryClient()
    val params = useParams()
    val GroupID = params["groupId"] ?: "Error"
    val SubgroupName = params["subgroupname"] ?: "Error"

    val queryFlows = useQuery<Any, QueryError, String, Any>(
        "FlowsInSubgroup", {
            fetchText(
                url = "$groupsPath/$GroupID/$SubgroupName/flows",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    val queryFlowsSelect = useQuery<Any, QueryError, String, Any>(
        "FlowsInSubgroupSelect", {
            fetchText(
                url = "$groupsPath/$GroupID/$SubgroupName/Select",
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    class MutationData(
        val qq: forString
    )


    val addSubgroupInFlowMutation = useMutation<Any, Any, MutationData, Any>(
        { newSubroupInFlow ->
            axios<String>(jso {
                url = "$groupsPath/${GroupID}/${SubgroupName}/flowInSubgroup"
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(newSubroupInFlow.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("FlowsInSubgroup")
                queryClient.invalidateQueries<Any>("FlowsInSubgroupSelect")
            }
        }
    )

    val deleteSubgroupInFlowMutation = useMutation<Any, Any, MutationData, Any>(
        { i ->
            axios<String>(jso {
                url = "$groupsPath/${GroupID}/${SubgroupName}/flowInSubgroup/delete"
                method = "Put"
                headers = json("Content-Type" to "application/json")
                data = JSON.stringify(i.qq)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("FlowsInSubgroup")
                queryClient.invalidateQueries<Any>("FlowsInSubgroupSelect")
            }
        }
    )


    if (queryFlows.isLoading or queryFlowsSelect.isLoading) div { +"Loading.." }
    else if (queryFlows.isError or queryFlowsSelect.isError) div { +"Error!!" }
    else {
        val items: Set<String> = Json.decodeFromString(queryFlows.data ?: "")
        val itemsSelect: Set<String> = Json.decodeFromString(queryFlowsSelect.data ?: "")
        child(fcSubgroup()) {
            attrs.Flows = items
            attrs.FlowsForSubgroup = itemsSelect
            attrs.addFlowInSubgroup = { n ->
                addSubgroupInFlowMutation.mutate(MutationData(forString(n)), null)
            }
            attrs.deleteFlow = { n ->
                deleteSubgroupInFlowMutation.mutate(MutationData(forString(n)), null)
            }
        }
    }
}