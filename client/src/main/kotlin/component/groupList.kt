package component

import kotlinext.js.jso
import kotlinx.html.INPUT
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
import ru.altmanea.edu.server.model.Config.Companion.groupsPath
import ru.altmanea.edu.server.model.Group
import ru.altmanea.edu.server.model.Item
import wrappers.QueryError
import wrappers.axios
import wrappers.fetchText
import kotlin.js.json

external interface GroupListProps : Props {
    var groups: List<Item<Group>>
    var deleteGroup: (Int) -> Unit
    var addGroup: (String) -> Unit

}

fun fcGroupList() = fc("GroupList") { props: GroupListProps ->
    val nameGroupRef = useRef<INPUT>()


    h3 { +"Groups" }
    ol("rectangle") {
        props.groups.mapIndexed { index, groupItem ->
            li {
                val group = groupItem.elem.name
                Link {
                    attrs.to = groupItem.uuid
                    +group
                }
                button {
                    +"X"
                    attrs.onClickFunction = {
                        props.deleteGroup(index)
                    }
                }
            }
        }
    }
    span {
        p("enterText") {
            +"Имя группы:"
            input {
                ref = nameGroupRef
            }
        }
        button {
            +"Add group"
            attrs.onClickFunction = {
                nameGroupRef.current?.value?.let { nameGroup ->
                    if (nameGroup != "") {
                        props.addGroup(nameGroup)
                    }
                }
            }
        }
    }


}

@Serializable
class GroupClient(
    override val elem: Group,
    override val etag: Long,
    override val uuid: String
) : Item<Group>

fun fcContainerGroupList() = fc("QueryGroupList") { _: Props ->
    val queryClient = useQueryClient()

    val query = useQuery<Any, QueryError, String, Any>(
        "GroupList", {
            fetchText(
                url = groupsPath,
                jso {
                    headers = json("Content-Type" to "application/json")
                }
            )
        }
    )

    val deleteGroupMutation = useMutation<Any, Any, Any, Any>(
        { GroupItem: Item<Group> ->
            axios<String>(jso {
                url = groupsPath + GroupItem.uuid
                method = "Delete"
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("GroupList")
            }
        }
    )


    val addGroupMutation = useMutation<Any, Any, Any, Any>(
        { group: Group ->
            axios<String>(jso {
                url = groupsPath
                method = "Post"
                headers = json(
                    "Content-Type" to "application/json"
                )
                data = Json.encodeToString(group)
            })
        },
        options = jso {
            onSuccess = { _: Any, _: Any, _: Any? ->
                queryClient.invalidateQueries<Any>("GroupList")
            }
        }
    )

    if (query.isLoading) div { +"Loading.." }
    else {
        val items: List<GroupClient> = Json.decodeFromString(query.data ?: "")
        child(fcGroupList()) {
            attrs.groups = items
            attrs.deleteGroup = {
                deleteGroupMutation.mutate(items[it], null)
            }
            attrs.addGroup = { n ->
                addGroupMutation.mutate(Group(n), null)
            }
        }
    }
}

//    if (query.isLoading) div { +"Loading .." }
//    else if (query.isError) div { +"Error!" }
//    else {
//        val groupItem = query.data?.toList() ?: emptyList()
//        child(fcGroupList()) {
//            attrs.groups = groupItem
//        }
//    }
