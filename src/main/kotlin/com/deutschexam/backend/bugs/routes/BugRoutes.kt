package com.deutschexam.backend.bugs.routes

import com.deutschexam.backend.bugs.model.BugReportRequest
import com.deutschexam.backend.bugs.repository.BugRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bugRoutes(bugRepo: BugRepository) {
    post("/bugs") {
        val req = call.receive<BugReportRequest>()
        val result = bugRepo.create(req.title, req.description)
        call.respond(HttpStatusCode.Created, result)
    }
}
