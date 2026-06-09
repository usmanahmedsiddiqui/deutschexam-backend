package com.deutschexam.backend.exams.routes

import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.util.ValidationException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.examRoutes(examDetailRepo: ExamDetailRepository, examRepo: ExamRepository) {
    route("/exam-details") {
        get {
            call.respond(HttpStatusCode.OK, examDetailRepo.findAll())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: throw ValidationException("Exam detail id is required.")
            val detail = examDetailRepo.findById(id) ?: throw ValidationException("Exam detail not found.")
            call.respond(HttpStatusCode.OK, detail)
        }
    }

    route("/exams") {
        get {
            val examDetailId = call.request.queryParameters["exam_detail_id"]
            val result = if (examDetailId != null) {
                examRepo.findByDetailId(examDetailId)
            } else {
                examRepo.findAll()
            }
            call.respond(HttpStatusCode.OK, result)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: throw ValidationException("Exam id is required.")
            val exam = examRepo.findById(id) ?: throw ValidationException("Exam not found.")
            call.respond(HttpStatusCode.OK, exam)
        }
    }
}
