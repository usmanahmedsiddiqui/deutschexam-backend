package com.deutschexam.backend.exams.routes

import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.util.ValidationException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.examRoutes(examDetailRepo: ExamDetailRepository, examRepo: ExamRepository) {

    // Returns the exam detail template for a given provider + level.
    // Example: GET /exam-details?provider_id=telc&level_id=a1
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

    // Returns a specific exam detail by its direct id.
    // Example: GET /exam-details/telc_a1
    get("/exam-details/{id}") {
        val id = call.parameters["id"] ?: throw ValidationException("Exam detail id is required.")
        val detail = examDetailRepo.findById(id) ?: throw ValidationException("Exam detail not found.")
        call.respond(HttpStatusCode.OK, detail)
    }

    // Returns the list of available exams for a provider + level.
    // Each item includes exam metadata + the exam detail structure (no questions).
    // Example: GET /exams?provider_id=telc&level_id=a1
    get("/exams") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            call.respond(HttpStatusCode.OK, examRepo.findByProviderAndLevel(providerId, levelId))
        } else {
            call.respond(HttpStatusCode.OK, examRepo.findAll())
        }
    }

    // Returns the full exam with all questions.
    // Example: GET /exams/telc_a1_01
    get("/exams/{id}") {
        val id = call.parameters["id"] ?: throw ValidationException("Exam id is required.")
        val exam = examRepo.findById(id) ?: throw ValidationException("Exam not found.")
        call.respond(HttpStatusCode.OK, exam)
    }
}
