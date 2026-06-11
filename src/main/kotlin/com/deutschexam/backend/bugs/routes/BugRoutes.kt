package com.deutschexam.backend.bugs.routes

import com.deutschexam.backend.bugs.model.BugReportRequest
import com.deutschexam.backend.bugs.repository.BugRepository
import com.deutschexam.backend.plugins.RATE_LIMIT_BUGS
import com.deutschexam.backend.util.ValidationException
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bugRoutes(bugRepo: BugRepository) {
    rateLimit(RateLimitName(RATE_LIMIT_BUGS)) {
        post("/bugs") {
            val req = call.receive<BugReportRequest>()
            if (req.title.isBlank()) throw ValidationException("Title is required.")
            if (req.description.isBlank()) throw ValidationException("Description is required.")
            val result = bugRepo.create(req.title, req.description)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
