package com.deutschexam.backend.bugs

import com.deutschexam.backend.bugs.model.BugReportRequest
import com.deutschexam.backend.bugs.model.BugReportResponse
import com.deutschexam.backend.bugs.repository.BugRepository
import com.deutschexam.backend.bugs.routes.bugRoutes
import com.deutschexam.backend.plugins.configureRateLimit
import com.deutschexam.backend.plugins.configureStatusPages
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientCN
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerCN
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class BugRoutesTest {

    private val bugRepo = mockk<BugRepository>()

    private fun testApp(block: suspend (io.ktor.client.HttpClient) -> Unit) = testApplication {
        install(ServerCN) { json(Json { ignoreUnknownKeys = true }) }
        application {
            configureRateLimit()
            configureStatusPages()
        }
        routing { bugRoutes(bugRepo) }

        val client = createClient {
            install(ClientCN) { json(Json { ignoreUnknownKeys = true }) }
        }
        block(client)
    }

    @Test
    fun `valid bug report returns 201 with the created report`() = testApp { client ->
        every { bugRepo.create("Crash on login", "App crashes when tapping login") } returns
            BugReportResponse(id = "abc-123", title = "Crash on login", description = "App crashes when tapping login")

        val response = client.post("/bugs") {
            contentType(ContentType.Application.Json)
            setBody(BugReportRequest(title = "Crash on login", description = "App crashes when tapping login"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<BugReportResponse>()
        assertEquals("abc-123", body.id)
        assertEquals("Crash on login", body.title)
    }

    @Test
    fun `blank title returns 422`() = testApp { client ->
        val response = client.post("/bugs") {
            contentType(ContentType.Application.Json)
            setBody(BugReportRequest(title = "   ", description = "Some description"))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `blank description returns 422`() = testApp { client ->
        val response = client.post("/bugs") {
            contentType(ContentType.Application.Json)
            setBody(BugReportRequest(title = "Some title", description = ""))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `empty title returns 422`() = testApp { client ->
        val response = client.post("/bugs") {
            contentType(ContentType.Application.Json)
            setBody(BugReportRequest(title = "", description = "Some description"))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }
}
