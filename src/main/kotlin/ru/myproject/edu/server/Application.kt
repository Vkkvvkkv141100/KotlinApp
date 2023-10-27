package ru.myproject.edu.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.css.*
import ru.altmanea.edu.server.model.Config
//import ru.altmanea.edu.server.repo.*
import ru.myproject.edu.server.rest.flow
import ru.myproject.edu.server.rest.group
import ru.myproject.edu.server.repo.flowsRepo
import ru.myproject.edu.server.repo.flowsRepoTestData
import ru.myproject.edu.server.repo.groupsRepo
import ru.myproject.edu.server.repo.groupsRepoTestData


fun main() {
    embeddedServer(
        Netty,
        port = Config.serverPort,
        host = Config.serverDomain,
        watchPaths = listOf("classes", "resources")
    ) {
        main()
    }.start(wait = true)
}

fun Application.main(test: Boolean = true) {
    if(test) {
        groupsRepoTestData.forEach { groupsRepo.create(it) }
        flowsRepoTestData.forEach { flowsRepo.create(it)}
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        index()
        group()
        flow()
    }
}