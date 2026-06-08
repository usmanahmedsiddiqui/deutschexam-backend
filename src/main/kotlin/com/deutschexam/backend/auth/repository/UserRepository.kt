package com.deutschexam.backend.auth.repository

import com.deutschexam.backend.auth.model.UserRecord
import com.deutschexam.backend.db.tables.RefreshTokensTable
import com.deutschexam.backend.db.tables.UserProductsTable
import com.deutschexam.backend.db.tables.UsersTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.time.Duration.Companion.days

private const val REFRESH_TOKEN_EXPIRY_DAYS = 30

class UserRepository(private val db: Database) {

    fun findById(id: String): UserRecord? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(id) }
            .firstOrNull()
            ?.toUserRecord()
    }

    fun findOrCreate(googleId: String, email: String, name: String, profilePicture: String?): UserRecord = transaction(db) {
        val existing = UsersTable.selectAll()
            .where { UsersTable.googleId eq googleId }
            .firstOrNull()

        val userId = if (existing != null) {
            UsersTable.update({ UsersTable.id eq existing[UsersTable.id] }) {
                it[UsersTable.name] = name
                it[UsersTable.profilePicture] = profilePicture
            }
            existing[UsersTable.id]
        } else {
            UsersTable.insert {
                it[UsersTable.googleId] = googleId
                it[UsersTable.name] = name
                it[UsersTable.email] = email
                it[UsersTable.profilePicture] = profilePicture
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

    // ── Refresh tokens ───────────────────────────────────────────────────────

    fun createRefreshToken(userId: String): Pair<String, Long> = transaction(db) {
        val token = UUID.randomUUID().toString()
        val expiresAt = Clock.System.now().plus(REFRESH_TOKEN_EXPIRY_DAYS.days)

        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = UUID.fromString(userId)
            it[RefreshTokensTable.token] = token
            it[RefreshTokensTable.expiresAt] = expiresAt
        }

        Pair(token, expiresAt.epochSeconds)
    }

    fun findUserByRefreshToken(token: String): UserRecord? = transaction(db) {
        val now = Clock.System.now()
        val row = RefreshTokensTable
            .selectAll()
            .where { (RefreshTokensTable.token eq token) and (RefreshTokensTable.expiresAt greater now) }
            .firstOrNull() ?: return@transaction null

        UsersTable.selectAll()
            .where { UsersTable.id eq row[RefreshTokensTable.userId] }
            .firstOrNull()
            ?.toUserRecord()
    }

    fun deleteRefreshToken(token: String) = transaction(db) {
        val tokenValue = token
        RefreshTokensTable.deleteWhere {
            with(SqlExpressionBuilder) { RefreshTokensTable.token eq tokenValue }
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
            profilePicture = this[UsersTable.profilePicture],
            ownedProductIds = ownedIds,
        )
    }
}
