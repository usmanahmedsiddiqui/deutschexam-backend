package com.deutschexam.backend.products.service

import com.deutschexam.backend.products.model.BuyProductResponseDto
import com.deutschexam.backend.products.repository.ProductRepository
import com.deutschexam.backend.products.repository.UserProductRepository
import com.deutschexam.backend.util.ApiErrorCode
import com.deutschexam.backend.util.ConflictException
import com.deutschexam.backend.util.NotFoundException

/**
 * Owns the purchase flow. Kept separate from [ProductRepository] so business rules
 * (ownership, conflict handling, and — in future — payment verification) live in one place.
 */
class PurchaseService(
    private val productRepo: ProductRepository,
    private val userProductRepo: UserProductRepository,
) {

    fun buyProduct(productId: String, userId: String): BuyProductResponseDto {
        val product = productRepo.findById(productId)
            ?: throw NotFoundException(code = ApiErrorCode.PRODUCT_NOT_FOUND, message = "Product not found.")

        if (productId in userProductRepo.getOwnedProductIds(userId)) {
            throw ConflictException(
                code = ApiErrorCode.PRODUCT_ALREADY_OWNED,
                message = "You already own this product.",
            )
        }

        // TODO(SEC-2): verify a real payment (Google Play Billing / Stripe) BEFORE granting
        // ownership. Until billing is integrated this endpoint must NOT be exposed to real
        // users — it currently grants ownership for free.
        userProductRepo.addOwnedProduct(userId, productId)

        return BuyProductResponseDto(product)
    }
}
