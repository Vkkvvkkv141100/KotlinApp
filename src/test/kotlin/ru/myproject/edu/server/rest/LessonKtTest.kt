package ru.myproject.edu.server.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import ru.myproject.edu.server.main
import ru.altmanea.edu.server.model.Config
import ru.altmanea.edu.server.model.Flow
import ru.altmanea.edu.server.model.Group
import ru.myproject.edu.server.repo.RepoItem
import kotlin.test.assertEquals

internal class LessonKtTest {

    @Test
    fun testLessonRoute() {
        withTestApplication(Application::main) {

            val groupItems = handleRequest(HttpMethod.Get, Config.groupsPath).run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<List<RepoItem<Group>>>()
            }
            assertEquals(4, groupItems.size)

            val g29m = groupItems.find { it.elem.name == "29m" }
            check(g29m != null)

            val subgroups = handleRequest(HttpMethod.Get, Config.groupsPath + g29m.elem.name).run{
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<List<RepoItem<Group>>>()
            }

            val flowItems = handleRequest(HttpMethod.Get, Config.flowsPath).run {
                assertEquals(HttpStatusCode.OK, response.status())
                decodeBody<List<RepoItem<Flow>>>()
            }
            assertEquals(4, groupItems.size)
        }
    }
}

