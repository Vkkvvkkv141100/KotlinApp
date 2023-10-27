package ru.myproject.edu.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.altmanea.edu.server.model.Config.Companion.groupsPath
import ru.altmanea.edu.server.model.Flow
import ru.altmanea.edu.server.model.Group
import ru.altmanea.edu.server.model.forString
import ru.myproject.edu.server.repo.RepoItem
import ru.myproject.edu.server.repo.flowsRepo
import ru.myproject.edu.server.repo.groupsRepo

fun Route.group() =
    route(groupsPath) {

        //Выгрузка всех групп
        get {
            if (!groupsRepo.isEmpty()) {
                call.respond(groupsRepo.findAll())
            } else {
                call.respondText("no groups found")
            }
        }

        //Получение имени подгруппы
        get("{groupId}/name") {
            val id = call.parameters["groupId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val GroupItems = groupsRepo[id] ?: return@get call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(GroupItems.elem.name)
        }

        //Выгрузка потоков группы
        get("{groupId}/flows") {
            val id = call.parameters["groupId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val GroupItems = groupsRepo[id] ?: return@get call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )
            val nameGroup = GroupItems.elem.name
            var flowsInGroup: Set<String> = emptySet()

            val allFlows = flowsRepo.findAll()
            allFlows.forEach { flow ->
                flow.elem.participants.forEach { part ->
                    if (nameGroup == part) {
                        flowsInGroup = flowsInGroup + flow.elem.name
                    }
                }
            }
            call.respond(flowsInGroup)
        }

        //Потоки для группы для селекта
        get("{groupId}/group/Select") {
            val id = call.parameters["groupId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val GroupItems = groupsRepo[id] ?: return@get call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )

            val nameGroup = GroupItems.elem.name
            var flowsForGroup: Set<String> = emptySet()

            val allFlows = flowsRepo.findAll()
            allFlows.forEach { flow ->
                if (nameGroup !in flow.elem.participants) {
                    if (flow.elem.type == "Lecture" || (flow.elem.type == "Practice" && flow.elem.participants.isEmpty())) {
                        flowsForGroup = flowsForGroup + flow.elem.name
                    }
                }
            }
            call.respond(flowsForGroup)
        }

        //Добавление потока в группу
        put("{groupId}/group/flowInGroup") {
            val id = call.parameters["groupId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val GroupItem = groupsRepo[id] ?: return@put call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )
            val nameGroup = GroupItem.elem.name

            val newPartClient = call.receive<forString>()
            var neededFlow = RepoItem<Flow>(Flow("null", "null", setOf("null")), "String", 123)
            val allFlow = flowsRepo.findAll()
            allFlow.forEach { it ->
                if (it.elem.name == newPartClient.name) {
                    neededFlow = it
                }
            }
            val oldNameFlow = neededFlow.elem.name
            val oldPart = neededFlow.elem.participants
            val oldType = neededFlow.elem.type

            val newPart = oldPart + nameGroup

            val flowWithNewPart = Flow(oldNameFlow, oldType, newPart)

            flowsRepo.update(neededFlow.uuid, flowWithNewPart)
            call.respondText(
                "Participants add correctly",
                status = HttpStatusCode.Accepted
            )
        }

        //Удаление потока из группы
        put("{groupId}/group/deleteFlowInGroup") {
            val id = call.parameters["groupId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val GroupItem = groupsRepo[id] ?: return@put call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )
            val nameGroup = GroupItem.elem.name

            val newPartClient = call.receive<forString>()
            var neededFlow = RepoItem<Flow>(Flow("null", "null", setOf("null")), "String", 123)
            val allFlow = flowsRepo.findAll()
            allFlow.forEach { it ->
                if (it.elem.name == newPartClient.name) {
                    neededFlow = it
                }
            }
            val oldNameFlow = neededFlow.elem.name
            val oldPart = neededFlow.elem.participants
            val oldType = neededFlow.elem.type

            val newPart = oldPart - nameGroup

            val flowWithNewPart = Flow(oldNameFlow, oldType, newPart)

            flowsRepo.update(neededFlow.uuid, flowWithNewPart)
            call.respondText(
                "Participants add correctly",
                status = HttpStatusCode.Accepted
            )
        }


        //Выгрузка потоков подгруппы
        get("{groupId}/{subgroupname}/flows") {
            val id = call.parameters["groupId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val name = call.parameters["subgroupname"] ?: return@get call.respondText(
                "Missing or malformed subgroup name",
                status = HttpStatusCode.BadRequest
            )
            var flowsInSubgroup: Set<String> = emptySet()
            val allFlows = flowsRepo.findAll()
            allFlows.forEach { flow ->
                flow.elem.participants.forEach { part ->
                    if (name == part) {
                        flowsInSubgroup = flowsInSubgroup + flow.elem.name
                    }
                }
            }
            call.respond(flowsInSubgroup)
        }

        //Потоки для селекта подгруппы
        get("{groupId}/{subgroupname}/Select") {
            val name = call.parameters["subgroupname"] ?: return@get call.respondText(
                "Missing or malformed subgroup name",
                status = HttpStatusCode.BadRequest
            )
            var flowsForSubgroup: Set<String> = emptySet()
            val allFlows = flowsRepo.findAll()
            allFlows.forEach { flow ->
                if (name !in flow.elem.participants) {
                    if (flow.elem.type == "Lab" && flow.elem.participants.isEmpty()) {
                        flowsForSubgroup = flowsForSubgroup + flow.elem.name
                    }
                }
            }
            call.respond(flowsForSubgroup)
        }

        //Добавление потока в подгруппу
        put("{groupId}/{subgroupname}/flowInSubgroup") {
            val name = call.parameters["subgroupname"] ?: return@put call.respondText(
                "Missing or malformed subgroup name",
                status = HttpStatusCode.BadRequest
            )
            val newPartClient = call.receive<forString>()
            var neededFlow = RepoItem<Flow>(Flow("null", "null", setOf("null")), "String", 123)
            val allFlow = flowsRepo.findAll()
            allFlow.forEach { it ->
                if (it.elem.name == newPartClient.name) {
                    neededFlow = it
                }
            }
            val oldNameFlow = neededFlow.elem.name
            val oldPart = neededFlow.elem.participants
            val oldType = neededFlow.elem.type

            val newPart = oldPart + name
            if (neededFlow.elem.participants.size < 1) {
                val flowWithNewPart = Flow(oldNameFlow, oldType, newPart)
                flowsRepo.update(neededFlow.uuid, flowWithNewPart)
                call.respondText(
                    "Participants add correctly",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "Participants not added"
                )
            }
        }

        //Удаление потока из подгруппу
        put("{groupId}/{subgroupname}/flowInSubgroup/delete") {
            val name = call.parameters["subgroupname"] ?: return@put call.respondText(
                "Missing or malformed subgroup name",
                status = HttpStatusCode.BadRequest
            )
            val newPartClient = call.receive<forString>()
            var neededFlow = RepoItem<Flow>(Flow("null", "null", setOf("null")), "String", 123)
            val allFlow = flowsRepo.findAll()
            allFlow.forEach { it ->
                if (it.elem.name == newPartClient.name) {
                    neededFlow = it
                }
            }
            val oldNameFlow = neededFlow.elem.name
            val oldPart = neededFlow.elem.participants
            val oldType = neededFlow.elem.type

            val newPart = oldPart - name

            val flowWithNewPart = Flow(oldNameFlow, oldType, newPart)
            flowsRepo.update(neededFlow.uuid, flowWithNewPart)
            call.respondText(
                "Participants add correctly",
                status = HttpStatusCode.Accepted
            )
        }

        //Добавлеие группы
        post {
            var a = 0
            val group = call.receive<Group>()
            val allGroups = groupsRepo.findAll()
            allGroups.forEach { Group ->
                if (Group.elem.name == group.name) {
                    a++
                }
            }
            if  (a == 0) {
                groupsRepo.create(group)
            }
            call.respondText("Group stored correctly", status = HttpStatusCode.Created)
        }

        //Удаление группы
        delete("{groupId}") {
            val id = call.parameters["groupId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (groupsRepo.delete(id)) {
                call.respondText("groups removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found $id + ${groupsRepo.delete(id)}", status = HttpStatusCode.NotFound)
            }
        }


        //Изменение имени группы
        put("{groupId}/name") {
            val id = call.parameters["groupId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val group = groupsRepo[id] ?: return@put call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )
            val oldName = group.elem.name
            val oldSubgroup = group.elem.subgroups

            val newNameClient = call.receive<forString>()

            val groupWitnNewName = Group(newNameClient.name, oldSubgroup)
            groupsRepo.update(id, groupWitnNewName)
            call.respondText(
                "Subgroup add correctly",
                status = HttpStatusCode.Created
            )
        }

        //Выгрузка подгрупп группы
        get("{groupId}") {
            val id = call.parameters["groupId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val GroupItems = groupsRepo[id] ?: return@get call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(GroupItems.elem.subgroups)
        }

        //Добавление подгруппы
        put("{groupId}") {
            val id = call.parameters["groupId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val group = groupsRepo[id] ?: return@put call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )

            val oldNameGroup = group.elem.name
            val oldSubgroup = group.elem.subgroups

            val newSubgroupClient = call.receive<forString>()
            val newSubgroup = oldSubgroup + newSubgroupClient.name

            val groupWitnNewSubgroup = Group(oldNameGroup, newSubgroup)
            groupsRepo.update(id, groupWitnNewSubgroup)
            call.respondText(
                "Subgroup add correctly",
                status = HttpStatusCode.Created
            )
        }

        //Удаление подгруппы
        put("{groupId}/delete") {
            val id = call.parameters["groupId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val group = groupsRepo[id] ?: return@put call.respondText(
                "No group with id $id",
                status = HttpStatusCode.NotFound
            )

            val oldNameGroup = group.elem.name
            val oldSubgroup = group.elem.subgroups

            val indexDeleteSubgroupClient = call.receive<Int>()
            val newGroup = oldSubgroup.minus(oldSubgroup.elementAt(indexDeleteSubgroupClient))

            val groupWithoutSubgroup = Group(oldNameGroup, newGroup)
            groupsRepo.update(id, groupWithoutSubgroup)
            call.respondText(
                "Subgroup remove correctly",
                status = HttpStatusCode.Created
            )
        }

        //Выгрузка ВСЕХ подгрупп
        get("all") {
            var allsubgroups: Set<String> = emptySet()
            groupsRepo.findAll().forEach {
                allsubgroups = allsubgroups + it.elem.subgroups
            }
            call.respond(allsubgroups)
        }
    }



