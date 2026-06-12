package com.deutschexam.backend.exams.service

import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.products.repository.ProductRepository
import com.deutschexam.backend.products.repository.UserProductRepository
import com.deutschexam.backend.util.ForbiddenException
import com.deutschexam.backend.util.NotFoundException
import com.deutschexam.backend.util.TokenMissingException
import kotlinx.serialization.json.JsonElement

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

    /**
     * @param examId the requested exam id
     * @param userId the authenticated user id, or null for a guest
     * @return the full exam content if the caller is allowed to see it
     * @throws NotFoundException if the exam does not exist
     * @throws AuthException (TOKEN_MISSING) if a paid exam is requested by a guest
     * @throws ForbiddenException (EXAM_NOT_OWNED) if a paid exam is requested by a non-owner
     */
    fun getExamContent(examId: String, userId: String?): JsonElement {
        val exam = examRepo.findById(examId)
            ?: throw NotFoundException("Exam not found.", code = "EXAM_NOT_FOUND")

        if (exam.isFree) return exam.data

        if (userId == null) throw TokenMissingException()

        val owned = userProductRepo.getOwnedProductIds(userId)
        val productsForLevel = productRepo.findProductIdsByLevel(exam.levelId)

        if (owned.none { it in productsForLevel }) {
            throw ForbiddenException(
                "You do not own the product required to access this exam.",
                code = "EXAM_NOT_OWNED",
            )
        }

        return exam.data
    }
}
