package com.deutschexam.backend.auth.repository

import com.deutschexam.backend.db.tables.RefreshTokensTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.time.Duration.Companion.days

private const val REFRESH_TOKEN_EXPIRY_DAYS = 30

/** Owns the lifecycle of refresh tokens: issue, resolve-to-user, and revoke. */
class RefreshTokenRepository(private val db: Database) {

    /** Issues a new refresh token for [userId]; returns the token and its expiry epoch seconds. */
    fun create(userId: String): Pair<String, Long> = transaction(db) {
        val token = UUID.randomUUID().toString()
        val expiresAt = Clock.System.now().plus(REFRESH_TOKEN_EXPIRY_DAYS.days)

        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = UUID.fromString(userId)
            it[RefreshTokensTable.token] = token
            it[RefreshTokensTable.expiresAt] = expiresAt
        }

        Pair(token, expiresAt.epochSeconds)
    }

    /** Returns the owning user id for a valid, unexpired [token], or null otherwise. */
    fun findUserIdByToken(token: String): String? = transaction(db) {
        val now = Clock.System.now()
        RefreshTokensTable
            .selectAll()
            .where { (RefreshTokensTable.token eq token) and (RefreshTokensTable.expiresAt greater now) }
            .firstOrNull()
            ?.get(RefreshTokensTable.userId)
            ?.toString()
    }

    fun delete(token: String) = transaction(db) {
        RefreshTokensTable.deleteWhere { with(SqlExpressionBuilder) { RefreshTokensTable.token eq token } }
    }
}
