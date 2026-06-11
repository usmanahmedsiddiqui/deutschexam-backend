package com.deutschexam.backend.products.service

import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.products.model.ProductDto
import com.deutschexam.backend.products.repository.ProductRepository
import com.deutschexam.backend.products.repository.UserProductRepository
import com.deutschexam.backend.util.ConflictException
import com.deutschexam.backend.util.NotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PurchaseServiceTest {

    private val productRepo = mockk<ProductRepository>()
    private val userProductRepo = mockk<UserProductRepository>(relaxed = true)
    private val service = PurchaseService(productRepo, userProductRepo)

    private val product = ProductDto(
        id = "p_a1",
        level = LevelDto(id = "a1", name = "A1", description = "", shortDescription = "", catalog = emptyList()),
        priceCents = 1999,
        discountedPriceCents = null,
        currency = "EUR",
    )

    @Test
    fun `unknown product throws PRODUCT_NOT_FOUND`() {
        every { productRepo.findById("nope") } returns null
        val ex = assertFailsWith<NotFoundException> { service.buyProduct("nope", "u1") }
        assertEquals("PRODUCT_NOT_FOUND", ex.code)
    }

    @Test
    fun `buying an already-owned product throws PRODUCT_ALREADY_OWNED`() {
        every { productRepo.findById("p_a1") } returns product
        every { userProductRepo.getOwnedProductIds("u1") } returns listOf("p_a1")

        val ex = assertFailsWith<ConflictException> { service.buyProduct("p_a1", "u1") }
        assertEquals("PRODUCT_ALREADY_OWNED", ex.code)
        verify(exactly = 0) { userProductRepo.addOwnedProduct(any(), any()) }
    }

    @Test
    fun `buying a new product grants ownership`() {
        every { productRepo.findById("p_a1") } returns product
        every { userProductRepo.getOwnedProductIds("u1") } returns emptyList()

        val result = service.buyProduct("p_a1", "u1")

        assertEquals(product, result.product)
        verify(exactly = 1) { userProductRepo.addOwnedProduct("u1", "p_a1") }
    }
}
