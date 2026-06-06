package com.deutschexam.backend.auth.repository

import com.deutschexam.backend.auth.model.UserRecord
import com.deutschexam.backend.db.tables.UserProductsTable
import com.deutschexam.backend.db.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepository(private val db: Database) {

    fun findById(id: String): UserRecord? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(id) }
            .firstOrNull()
            ?.toUserRecord()
    }

    fun findOrCreate(googleId: String, email: String, name: String, pictureUrl: String?): UserRecord = transaction(db) {
        val existing = UsersTable.selectAll()
            .where { UsersTable.googleId eq googleId }
            .firstOrNull()

        val userId = if (existing != null) {
            UsersTable.update({ UsersTable.id eq existing[UsersTable.id] }) {
                it[UsersTable.name] = name
                it[UsersTable.pictureUrl] = pictureUrl
            }
            existing[UsersTable.id]
        } else {
            UsersTable.insert {
                it[UsersTable.googleId] = googleId
                it[UsersTable.name] = name
                it[UsersTable.email] = email
                it[UsersTable.pictureUrl] = pictureUrl
            }[UsersTable.id]
        }

        UsersTable.selectAll()
            .where { UsersTable.id eq userId }
            .first()
            .toUserRecord()
    }

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

    private fun ResultRow.toUserRecord(): UserRecord {
        val userId = this[UsersTable.id].toString()
        val ownedIds = UserProductsTable
            .selectAll()
            .where { UserProductsTable.userId eq this@toUserRecord[UsersTable.id] }
            .map { it[UserProductsTable.productId] }

        return UserRecord(
            id = userId,
            name = this[UsersTable.name],
            email = this[UsersTable.email],
            pictureUrl = this[UsersTable.pictureUrl],
            ownedProductIds = ownedIds,
        )
    }
}
