package com.deutschexam.backend.auth.repository

import com.deutschexam.backend.auth.model.UserRecord
import com.deutschexam.backend.db.tables.UserProductsTable
import com.deutschexam.backend.db.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepository(private val db: Database) {

    fun findByEmail(email: String): UserRecord? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.email eq email }
            .firstOrNull()
            ?.toUserRecord()
    }

    fun findById(id: String): UserRecord? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(id) }
            .firstOrNull()
            ?.toUserRecord()
    }

    fun existsByEmail(email: String): Boolean = transaction(db) {
        UsersTable.selectAll().where { UsersTable.email eq email }.count() > 0
    }

    fun create(name: String, gender: String, email: String, passwordHash: String): UserRecord = transaction(db) {
        val id = UsersTable.insert {
            it[UsersTable.name] = name
            it[UsersTable.email] = email
            it[UsersTable.gender] = gender
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.emailConfirmed] = false
        }[UsersTable.id]

        UserRecord(
            id = id.toString(),
            name = name,
            email = email,
            gender = gender,
            passwordHash = passwordHash,
            emailConfirmed = false,
            ownedProductIds = emptyList(),
        )
    }

    fun setEmailConfirmed(email: String) = transaction(db) {
        UsersTable.update({ UsersTable.email eq email }) {
            it[emailConfirmed] = true
        }
    }

    fun updatePassword(email: String, newHash: String) = transaction(db) {
        UsersTable.update({ UsersTable.email eq email }) {
            it[passwordHash] = newHash
        }
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
            gender = this[UsersTable.gender],
            passwordHash = this[UsersTable.passwordHash],
            emailConfirmed = this[UsersTable.emailConfirmed],
            ownedProductIds = ownedIds,
        )
    }
}
