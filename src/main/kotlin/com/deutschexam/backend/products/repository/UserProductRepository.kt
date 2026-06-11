package com.deutschexam.backend.products.repository

import com.deutschexam.backend.db.tables.UserProductsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

/**
 * Owns the user↔product ownership join. Lives in the products domain because ownership is a
 * product concern: it is consumed by the purchase flow and by exam access checks, not by auth.
 */
class UserProductRepository(private val db: Database) {

    fun getOwnedProductIds(userId: String): List<String> = transaction(db) {
        UserProductsTable
            .selectAll()
            .where { UserProductsTable.userId eq UUID.fromString(userId) }
            .map { it[UserProductsTable.productId] }
    }

    fun addOwnedProduct(userId: String, productId: String) = transaction(db) {
        UserProductsTable.insertIgnore {
            it[UserProductsTable.userId] = UUID.fromString(userId)
            it[UserProductsTable.productId] = productId
        }
    }
}
