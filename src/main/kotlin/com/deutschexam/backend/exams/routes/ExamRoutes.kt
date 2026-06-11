package com.deutschexam.backend.exams.routes

import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.exams.service.ExamAccessService
import com.deutschexam.backend.plugins.UserPrincipal
import com.deutschexam.backend.util.ValidationException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.examRoutes(
    examDetailRepo: ExamDetailRepository,
    examRepo: ExamRepository,
    examAccessService: ExamAccessService,
) {

    /**
     * GET /exam-details?provider_id=telc&level_id=a1
     * Returns the exam detail template (sections, tasks, passing criteria — no questions).
     *  Falls back to full list if no filters provided.
     */
    get("/exam-details") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            val detail = examDetailRepo.findByProviderAndLevel(providerId, levelId)
                ?: throw ValidationException("No exam detail found for provider '$providerId' and level '$levelId'.")
            call.respond(HttpStatusCode.OK, detail)
        } else {
            call.respond(HttpStatusCode.OK, examDetailRepo.findAll())
        }
    }

    /**
     *  GET /exam-details/telc_a1
     */
    get("/exam-details/{id}") {
        val id = call.parameters["id"] ?: throw ValidationException("Exam detail id is required.")
        val detail = examDetailRepo.findById(id) ?: throw ValidationException("Exam detail not found.")
        call.respond(HttpStatusCode.OK, detail)
    }

    /**
     *  GET /exams?provider_id=telc&level_id=a1
     *  Returns list of exams for a provider+level. Guest accessible.
     */
    get("/exams") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            call.respond(HttpStatusCode.OK, examRepo.findByProviderAndLevel(providerId, levelId))
        } else {
            call.respond(HttpStatusCode.OK, examRepo.findAll())
        }
    }

    /**
     *  GET /exams/telc_a1_01
     *  GET /exams/telc_a1_01
     *  Free exams: accessible by anyone. Paid exams: require a valid token AND product ownership.
     */
    authenticate("jwt-auth", optional = true) {
        get("/exams/{id}") {
            val id = call.parameters["id"] ?: throw ValidationException("Exam id is required.")
            val userId = call.principal<UserPrincipal>()?.userId
            val data = examAccessService.getExamContent(id, userId)
            call.respond(HttpStatusCode.OK, data)
        }
    }
}
