package com.deutschexam.backend.auth.repository

import com.deutschexam.backend.db.tables.RefreshTokensTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.util.UUID
import kotlin.time.Duration.Companion.days

private const val REFRESH_TOKEN_EXPIRY_DAYS = 30

data class TokenLookup(val userId: String, val isExpired: Boolean)

/** Owns the lifecycle of refresh tokens: issue, resolve-to-user, and revoke. */
class RefreshTokenRepository(private val db: Database) {

    /**
     * Issues a new refresh token for [userId].
     * The raw UUID is returned to the client; only its SHA-256 hash is persisted,
     * so a DB leak does not expose usable tokens (SEC-6).
     */
    fun create(userId: String): Pair<String, Long> = transaction(db) {
        val rawToken = UUID.randomUUID().toString()
        val expiresAt = Clock.System.now().plus(REFRESH_TOKEN_EXPIRY_DAYS.days)

        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = UUID.fromString(userId)
            it[RefreshTokensTable.token] = sha256(rawToken)
            it[RefreshTokensTable.expiresAt] = expiresAt
        }

        Pair(rawToken, expiresAt.epochSeconds)
    }

    /**
     * Looks up a refresh token by raw value.
     * Returns [TokenLookup] if the token exists (regardless of expiry), or null if not found.
     * Use [TokenLookup.isExpired] to distinguish `REFRESH_TOKEN_INVALID` from `REFRESH_TOKEN_EXPIRED`.
     */
    fun findToken(rawToken: String): TokenLookup? = transaction(db) {
        val row = RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.token eq sha256(rawToken) }
            .firstOrNull() ?: return@transaction null

        TokenLookup(
            userId = row[RefreshTokensTable.userId].toString(),
            isExpired = row[RefreshTokensTable.expiresAt] <= Clock.System.now(),
        )
    }

    fun delete(token: String) = transaction(db) {
        RefreshTokensTable.deleteWhere {
            with(SqlExpressionBuilder) { RefreshTokensTable.token eq sha256(token) }
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
