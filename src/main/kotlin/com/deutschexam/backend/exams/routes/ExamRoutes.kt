package com.deutschexam.backend.exams.routes

import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.exams.service.ExamAccessService
import com.deutschexam.backend.plugins.UserPrincipal
import com.deutschexam.backend.util.NotFoundException
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

    // GET /exam-details?provider_id=telc&level_id=a1
    get("/exam-details") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            val detail = examDetailRepo.findByProviderAndLevel(providerId, levelId)
                ?: throw NotFoundException(
                    "No exam detail found for provider '$providerId' and level '$levelId'.",
                    code = "EXAM_DETAIL_NOT_FOUND",
                )
            call.respond(HttpStatusCode.OK, detail)
        } else {
            call.respond(HttpStatusCode.OK, examDetailRepo.findAll())
        }
    }

    // GET /exam-details/{id}
    get("/exam-details/{id}") {
        val id = call.parameters["id"] ?: throw ValidationException("Exam detail id is required.")
        val detail = examDetailRepo.findById(id)
            ?: throw NotFoundException("Exam detail not found.", code = "EXAM_DETAIL_NOT_FOUND")
        call.respond(HttpStatusCode.OK, detail)
    }

    // GET /exams?provider_id=telc&level_id=a1
    get("/exams") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            call.respond(HttpStatusCode.OK, examRepo.findByProviderAndLevel(providerId, levelId))
        } else {
            call.respond(HttpStatusCode.OK, examRepo.findAll())
        }
    }

    // GET /exams/{id} — free exams open to all; paid exams require a token + product ownership
    authenticate("jwt-auth", optional = true) {
        get("/exams/{id}") {
            val id = call.parameters["id"] ?: throw ValidationException("Exam id is required.")
            val userId = call.principal<UserPrincipal>()?.userId
            val data = examAccessService.getExamContent(id, userId)
            call.respond(HttpStatusCode.OK, data)
        }
    }
}
