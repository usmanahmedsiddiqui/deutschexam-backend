package com.deutschexam.backend.exams.routes

import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.exams.service.ExamAccessService
import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.levels.repository.LevelRepository
import com.deutschexam.backend.plugins.UserPrincipal
import com.deutschexam.backend.providers.model.ProviderDto
import com.deutschexam.backend.providers.repository.ProviderRepository
import com.deutschexam.backend.util.ApiErrorCode
import com.deutschexam.backend.util.NotFoundException
import com.deutschexam.backend.util.ValidationException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

private val enrichJson = Json { encodeDefaults = true }

/**
 * The exam content stored in the DB only carries `provider_id` / `level_id`. The mobile client's
 * ExamDto expects the full `provider` and `level` objects instead, so we drop the raw id keys and
 * attach the resolved objects using the same shapes returned by GET /providers and GET /levels.
 */
private fun JsonElement.withProviderAndLevel(provider: ProviderDto?, level: LevelDto?): JsonElement =
    buildJsonObject {
        jsonObject
            .filterKeys { it != "provider_id" && it != "level_id" }
            .forEach { (key, value) -> put(key, value) }
        if (provider != null) put("provider", enrichJson.encodeToJsonElement(provider))
        if (level != null) put("level", enrichJson.encodeToJsonElement(level))
    }

fun Route.examRoutes(
    examDetailRepo: ExamDetailRepository,
    examRepo: ExamRepository,
    examAccessService: ExamAccessService,
    providerRepo: ProviderRepository,
    levelRepo: LevelRepository,
) {

    get("/exam-details") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            val detail = examDetailRepo.findByProviderAndLevel(providerId, levelId)
                ?: throw NotFoundException(
                    code = ApiErrorCode.EXAM_DETAIL_NOT_FOUND,
                    message = "No exam detail found for provider '$providerId' and level '$levelId'.",
                )
            call.respond(HttpStatusCode.OK, detail)
        } else {
            call.respond(HttpStatusCode.OK, examDetailRepo.findAll())
        }
    }

    get("/exam-details/{id}") {
        val id = call.parameters["id"] ?: throw ValidationException("Exam detail id is required.")
        val detail = examDetailRepo.findById(id)
            ?: throw NotFoundException(code = ApiErrorCode.EXAM_DETAIL_NOT_FOUND, message = "Exam detail not found.")
        call.respond(HttpStatusCode.OK, detail)
    }

    get("/exams") {
        val providerId = call.request.queryParameters["provider_id"]
        val levelId = call.request.queryParameters["level_id"]

        if (providerId != null && levelId != null) {
            val provider = providerRepo.findById(providerId)
            val level = levelRepo.findById(levelId)
            val exams = examRepo.findByProviderAndLevel(providerId, levelId)
                .map { it.withProviderAndLevel(provider, level) }
            call.respond(HttpStatusCode.OK, exams)
        } else {
            call.respond(HttpStatusCode.OK, examRepo.findAll())
        }
    }

    authenticate("jwt-auth", optional = true) {
        get("/exams/{id}") {
            val id = call.parameters["id"] ?: throw ValidationException("Exam id is required.")
            val userId = call.principal<UserPrincipal>()?.userId
            val data = examAccessService.getExamContent(id, userId)
            call.respond(HttpStatusCode.OK, data)
        }
    }
}
