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

private const val TITLE_MAX_LENGTH = 255
private const val DESCRIPTION_MAX_LENGTH = 5000

fun Route.bugRoutes(bugRepo: BugRepository) {
    rateLimit(RateLimitName(RATE_LIMIT_BUGS)) {
        post("/bugs") {
            val req = call.receive<BugReportRequest>()
            val title = req.title.trim()
            val description = req.description.trim()

            if (title.isBlank()) throw ValidationException("Title is required.")
            if (title.length > TITLE_MAX_LENGTH) throw ValidationException("Title must not exceed $TITLE_MAX_LENGTH characters.")
            if (description.isBlank()) throw ValidationException("Description is required.")
            if (description.length > DESCRIPTION_MAX_LENGTH) throw ValidationException("Description must not exceed $DESCRIPTION_MAX_LENGTH characters.")

            val result = bugRepo.create(title, description)
            call.respond(HttpStatusCode.Created, result)
        }
    }
}
