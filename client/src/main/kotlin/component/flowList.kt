package component

import kotlinext.js.jso
import kotlinx.html.INPUT
import kotlinx.html.SELECT
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.Props
import react.dom.*
import react.fc
import react.query.useMutation
import react.query.useQuery
import react.query.useQueryClient
import react.router.dom.Link
import react.useRef
import ru.altmanea.edu.server.model.Config
import ru.altmanea.edu.server.model.Config.Companion.flowsPath
import ru.altmanea.edu.server.model.Flow
import ru.altmanea.edu.server.model.Item
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface FlowListProps : Props {
    var flows: List<Item<Flow>>
    var deleteFlow: (Int) -> Unit
    var addFlow: (String, String) -> Unit
}

interface MySelect {
    val value: String
}

fun fcFlowList() = fc("FlowList") { props: FlowListProps ->
    val nameFlowRef = useRef<INPUT>()
    val selectRef = useRef<SELECT>()

    val forSelect = listOf<String>("Lecture", "Practice", "Lab")

    h3 { +"Flows " }
    ol("rectangle") {
        props.flows.mapIndexed { index, flowItem ->
            li {
                val flow = flowItem.elem.name
                Link {
                    attrs.to = flowItem.uuid
                    +flow
                }
                button {
                    +"x"
                    attrs.onClickFunction = {
                        props.deleteFlow(index)
                    }
                }
            }
        }
    }
    span {
        p("enterText") {
            +"Имя потока:"
            input {
                ref = nameFlowRef
            }
        }
        select("enterTextInput") {
            ref = selectRef

            forSelect.forEach {
                option {
                    +it
                    attrs.value = it
                }
            }
        }
        button {
            +"Добавить поток"
            attrs.onClickFunction = {
                val select = selectRef.current.unsafeCast<MySelect>()
                val type = select.value
                nameFlowRef.current?.value?.let { nameFlow ->
                    if (nameFlow != "") {
                        props.addFlow(nameFlow, type)
                    }
                }
            }
        }
    }
}

@Serializable
class FlowClient(
    override val elem: Flow,
    override val etag: Long,
    override val uuid: String
) : Item<Flow>


fun fcContainerFlowList() = fc("QueryFlowList") { _: Props ->
    val queryClient = useQueryClient()


    val query = useQuery<Any, QueryError, String, Any>(
        "FlowList1", {
            fetchText(
                url = flowsPath,
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    val deleteFlowMutation = useMutation<Any, Any, Any, Any>(
        { FlowItem: Item<Flow> ->
            axios<String>(jso {
                url = flowsPath + FlowItem.uuid
                method = "Delete"
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("FlowList1")
            }
        }
    )

    val addFlowMutation = useMutation<Any, Any, Any, Any>(
        { flow: Flow ->
            axios<String>(jso {
                url = Config.flowsPath
                method = "Post"
                headers = json(
                    "Content-Type" to "application/json"
                )
                data = Json.encodeToString(flow)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("FlowList1")
            }
        }
    )

    if (query.isLoading) div { +"Loading.." }
    else if (query.isError) div { +"Error!" }
    else {
        val items: List<FlowClient> = Json.decodeFromString(query.data ?: "")
        child(fcFlowList()) {
            attrs.flows = items
            attrs.deleteFlow = {
                deleteFlowMutation.mutate(items[it], null)
            }
            attrs.addFlow = { n, t ->
                addFlowMutation.mutate(Flow(n, type = t), null)

            }
        }
    }
}