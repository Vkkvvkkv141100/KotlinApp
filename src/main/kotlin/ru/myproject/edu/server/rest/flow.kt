package ru.myproject.edu.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.altmanea.edu.server.model.Flow
import ru.altmanea.edu.server.model.Config.Companion.flowsPath
//import ru.nikxor.edu.server.model.Flow
import ru.altmanea.edu.server.model.forString
import ru.myproject.edu.server.repo.flowsRepo
import ru.myproject.edu.server.repo.groupsRepo

fun Route.flow() =
    route(flowsPath) {

        //Выгрузка всех потоков
        get {
            if (!flowsRepo.isEmpty()) {
                call.respond(flowsRepo.findAll())
            } else {
                call.respondText("no flows found", status = HttpStatusCode.NotFound)
            }
        }


        //Добавлеие потока
        post {
            var a = 0
            val flow = call.receive<Flow>()
            val allFlows = flowsRepo.findAll()
            allFlows.forEach { Flow ->
                if (Flow.elem.name == flow.name) {
                    a++
                }
            }
            if (a == 0) {
                flowsRepo.create(flow)
            }
            call.respondText("Flow stored correctly", status = HttpStatusCode.Created)
        }

        //Удаление потока
        delete("{flowId}") {
            val id = call.parameters["flowId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (flowsRepo.delete(id)) {
                call.respondText("flow removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found $id + ${flowsRepo.delete(id)}", status = HttpStatusCode.NotFound)
            }
        }

        //Изменение имени потока
        put("{flowId}/name") {
            val id = call.parameters["flowId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val flow = flowsRepo[id] ?: return@put call.respondText(
                "No flow with id $id",
                status = HttpStatusCode.NotFound
            )

            val oldName = flow.elem.name
            val oldPart = flow.elem.participants
            val oldType = flow.elem.type

            val newNameClient = call.receive<forString>()

            val flowWitnNewName = Flow(newNameClient.name, oldType, oldPart)
            flowsRepo.update(id, flowWitnNewName)
            call.respondText(
                "Name update correctly",
                status = HttpStatusCode.Created
            )
        }

        //Выгрузка групп и подгрупп в потоке
        get("{flowId}") {
            val id = call.parameters["flowId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val FlowItems = flowsRepo[id] ?: return@get call.respondText(
                "No flow with id $id",
                status = HttpStatusCode.NotFound
            )

            var groupInFlows:Set<String> = emptySet()
            val allGroups = groupsRepo.findAll()
            allGroups.forEach { Group ->
                FlowItems.elem.participants.forEach {
                    if (Group.elem.name == it) {
                        groupInFlows = groupInFlows + Group.elem.name
                    }
                }
            }

            call.respond(groupInFlows)
        }

        //Выгрузка типа потока
        get("{flowId}/object") {
            val id = call.parameters["flowId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val FlowItem = flowsRepo[id] ?: return@get call.respondText(
                "No flow with id $id",
                status = HttpStatusCode.NotFound
            )

            call.respond(FlowItem)
        }

        //Добавление групп/подгрупп в поток
        put("{flowId}") {
            val id = call.parameters["flowId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val flow = flowsRepo[id] ?: return@put call.respondText(
                "No flows with id $id",
                status = HttpStatusCode.NotFound
            )
            val oldNameFlow = flow.elem.name
            val oldPart = flow.elem.participants
            val oldType = flow.elem.type

            val newPartClient = call.receive<forString>()
            val newPart = oldPart + newPartClient.name

            val flowWithNewPart = Flow(oldNameFlow, oldType, newPart)

            flowsRepo.update(id, flowWithNewPart)
            call.respondText(
                "Participants add correctly",
                status = HttpStatusCode.Accepted
            )
        }

        //Удаление членов потока
        put("{flowId}/delete") {
            val id = call.parameters["flowId"] ?: return@put call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.NotFound
            )
            val flow = flowsRepo[id] ?: return@put call.respondText(
                "No flow with id $id"
            )

            val oldNameFlow = flow.elem.name
            val oldPart = flow.elem.participants
            val oldType = flow.elem.type

            val indexDeleteClient = call.receive<Int>()
            val newFlow = oldPart.minus(oldPart.elementAt(indexDeleteClient))

            val flowWithoutPart = Flow(oldNameFlow, oldType, newFlow)

            flowsRepo.update(id, flowWithoutPart)
            call.respondText(
                "Part remove correctly",
                status = HttpStatusCode.Created
            )
        }

        //Выгрузка подгрупп для селекта
        get("{flowId}/Select") {
            var allsubgroups: Set<String> = emptySet()
            val id = call.parameters["flowId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val FlowItems = flowsRepo[id] ?: return@get call.respondText(
                "No flow with id $id",
                status = HttpStatusCode.NotFound
            )

            groupsRepo.findAll().forEach {
                allsubgroups = allsubgroups + it.elem.subgroups
            }
            val subgroupInFlow = allsubgroups - FlowItems.elem.participants
            call.respond(subgroupInFlow)
        }


        //Выгрузка групп для селекта
        get("{flowId}/SelectGroup") {
            val id = call.parameters["flowId"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val FlowItems = flowsRepo[id] ?: return@get call.respondText(
                "No flow with id $id",
                status = HttpStatusCode.NotFound
            )
            val qe: Set<String> = FlowItems.elem.participants
            var ee: Set<String> = emptySet()
            groupsRepo.findAll().forEach {
                ee = ee + it.elem.name
            }
            val qq = ee - qe
            call.respond(qq)
        }
    }