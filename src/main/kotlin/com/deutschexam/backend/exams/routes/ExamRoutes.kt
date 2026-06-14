package com.deutschexam.backend.exams.routes

import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.exams.service.ExamAccessService
import com.deutschexam.backend.plugins.UserPrincipal
import com.deutschexam.backend.util.ApiErrorCode
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
    get("/exam-details") {
        val providerId = call.request.queryParameters["provider_id"]
            ?: throw ValidationException("provider_id is required.")
        val levelId = call.request.queryParameters["level_id"]
            ?: throw ValidationException("level_id is required.")

        val detail = examDetailRepo.findByProviderAndLevel(providerId, levelId)
            ?: throw NotFoundException(
                code = ApiErrorCode.EXAM_DETAIL_NOT_FOUND,
                message = "No exam detail found for provider '$providerId' and level '$levelId'.",
            )
        call.respond(HttpStatusCode.OK, detail)
    }

    get("/exams") {
        val providerId = call.request.queryParameters["provider_id"]
            ?: throw ValidationException("provider_id is required.")
        val levelId = call.request.queryParameters["level_id"]
            ?: throw ValidationException("level_id is required.")

        call.respond(HttpStatusCode.OK, examRepo.findByProviderAndLevel(providerId, levelId))

    }

    authenticate("jwt-auth", optional = true) {
        get("/exams/{id}") {
            val id = call.parameters["id"] ?: throw ValidationException("Exam id is required.")
            val userId = call.principal<UserPrincipal>()?.userId
            call.respond(HttpStatusCode.OK, examAccessService.getExamContent(id, userId))
        }
    }
}
