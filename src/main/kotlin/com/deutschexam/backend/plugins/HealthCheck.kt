package com.deutschexam.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class HealthResponse(val status: String)

fun Application.configureHealthCheck(db: Database) {
    routing {
        get("/health") {
            val healthy = runCatching {
                transaction(db) { exec("SELECT 1") }
                true
            }.getOrElse {
                call.application.log.error("Health check DB probe failed", it)
                false
            }

            if (healthy) {
                call.respond(HttpStatusCode.OK, HealthResponse("ok"))
            } else {
                call.respond(HttpStatusCode.ServiceUnavailable, HealthResponse("unavailable"))
            }
        }
    }
}
