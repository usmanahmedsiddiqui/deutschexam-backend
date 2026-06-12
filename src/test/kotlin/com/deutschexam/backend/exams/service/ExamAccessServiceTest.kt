package com.deutschexam.backend.exams.service

import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.exams.repository.ExamResult
import com.deutschexam.backend.products.repository.ProductRepository
import com.deutschexam.backend.products.repository.UserProductRepository
import com.deutschexam.backend.util.ApiErrorCode
import com.deutschexam.backend.util.ForbiddenException
import com.deutschexam.backend.util.NotFoundException
import com.deutschexam.backend.util.TokenMissingException
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExamAccessServiceTest {

    private val examRepo = mockk<ExamRepository>()
    private val productRepo = mockk<ProductRepository>()
    private val userProductRepo = mockk<UserProductRepository>()
    private val service = ExamAccessService(examRepo, productRepo, userProductRepo)

    private val data = JsonPrimitive("exam-content")

    private fun exam(isFree: Boolean) =
        ExamResult(isFree = isFree, levelId = "a1", providerId = "telc", data = data)

    @Test
    fun `free exam is returned to a guest`() {
        every { examRepo.findById("e1") } returns exam(isFree = true)
        assertEquals(data, service.getExamContent("e1", userId = null))
    }

    @Test
    fun `unknown exam throws EXAM_NOT_FOUND`() {
        every { examRepo.findById("missing") } returns null
        val ex = assertFailsWith<NotFoundException> { service.getExamContent("missing", userId = null) }
        assertEquals(ApiErrorCode.EXAM_NOT_FOUND.name, ex.code)
    }

    @Test
    fun `paid exam without a token throws TOKEN_MISSING (SEC-1)`() {
        every { examRepo.findById("e1") } returns exam(isFree = false)
        val ex = assertFailsWith<TokenMissingException> { service.getExamContent("e1", userId = null) }
        assertEquals(ApiErrorCode.TOKEN_MISSING.name, ex.code)
    }

    @Test
    fun `paid exam for a non-owner throws EXAM_NOT_OWNED (SEC-1)`() {
        every { examRepo.findById("e1") } returns exam(isFree = false)
        every { userProductRepo.getOwnedProductIds("u1") } returns emptyList()
        every { productRepo.findProductIdsByLevel("a1") } returns setOf("p_a1")

        val ex = assertFailsWith<ForbiddenException> { service.getExamContent("e1", userId = "u1") }
        assertEquals(ApiErrorCode.EXAM_NOT_OWNED.name, ex.code)
    }

    @Test
    fun `paid exam for an owner is returned`() {
        every { examRepo.findById("e1") } returns exam(isFree = false)
        every { userProductRepo.getOwnedProductIds("u1") } returns listOf("p_a1")
        every { productRepo.findProductIdsByLevel("a1") } returns setOf("p_a1")

        assertEquals(data, service.getExamContent("e1", userId = "u1"))
    }

    @Test
    fun `owning a product for a different level does not grant access`() {
        every { examRepo.findById("e1") } returns exam(isFree = false)
        every { userProductRepo.getOwnedProductIds("u1") } returns listOf("p_b1")
        every { productRepo.findProductIdsByLevel("a1") } returns setOf("p_a1")

        assertFailsWith<ForbiddenException> { service.getExamContent("e1", userId = "u1") }
    }
}
