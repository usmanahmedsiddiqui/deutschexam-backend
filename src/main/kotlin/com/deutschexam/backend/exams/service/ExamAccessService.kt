package com.deutschexam.backend.exams.service

import com.deutschexam.backend.exams.model.ExamDto
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.products.repository.ProductRepository
import com.deutschexam.backend.products.repository.UserProductRepository
import com.deutschexam.backend.util.ApiErrorCode
import com.deutschexam.backend.util.ForbiddenException
import com.deutschexam.backend.util.NotFoundException
import com.deutschexam.backend.util.TokenMissingException

/**
 * Enforces access control for full exam content.
 *
 * Free exams are open to everyone. Paid exams require a valid token AND ownership of the
 * product that covers the exam's level — checking the JWT is present is not enough (SEC-1).
 */
class ExamAccessService(
    private val examRepo: ExamRepository,
    private val productRepo: ProductRepository,
    private val userProductRepo: UserProductRepository,
) {

    fun getExamContent(examId: String, userId: String?): ExamDto {
        val exam = examRepo.findById(examId)
            ?: throw NotFoundException(code = ApiErrorCode.EXAM_NOT_FOUND, message = "Exam not found.")

        if (exam.isFree) return exam

        if (userId == null) throw TokenMissingException()

        val owned = userProductRepo.getOwnedProductIds(userId)
        val productsForLevel = productRepo.findProductIdsByLevel(exam.level.id)

        if (owned.none { it in productsForLevel }) {
            throw ForbiddenException(
                code = ApiErrorCode.EXAM_NOT_OWNED,
                message = "You do not own the product required to access this exam.",
            )
        }

        return exam
    }
}
