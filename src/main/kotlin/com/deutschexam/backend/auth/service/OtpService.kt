package com.deutschexam.backend.auth.service

import com.deutschexam.backend.auth.model.OtpType
import com.deutschexam.backend.auth.model.OtpValidationResult
import com.deutschexam.backend.db.tables.OtpTable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class OtpService(private val db: Database) {

    fun generate(email: String, type: OtpType): String {
        val code = (1000..9999).random().toString()
        val expiresAt = Clock.System.now().plus(15.minutes)

        transaction(db) {
            OtpTable.update({
                (OtpTable.email eq email) and
                (OtpTable.type eq type.name) and
                (OtpTable.used eq false)
            }) { it[used] = true }

            OtpTable.insert {
                it[OtpTable.email] = email
                it[OtpTable.code] = code
                it[OtpTable.type] = type.name
                it[OtpTable.expiresAt] = expiresAt
            }
        }
        return code
    }

    fun validate(email: String, code: String, type: OtpType): OtpValidationResult {
        return transaction(db) {
            val row = OtpTable.selectAll()
                .where {
                    (OtpTable.email eq email) and
                    (OtpTable.code eq code) and
                    (OtpTable.type eq type.name) and
                    (OtpTable.used eq false)
                }
                .orderBy(OtpTable.createdAt, SortOrder.DESC)
                .firstOrNull() ?: return@transaction OtpValidationResult.Invalid

            if (row[OtpTable.expiresAt] < Clock.System.now()) {
                return@transaction OtpValidationResult.Expired
            }

            OtpTable.update({ OtpTable.id eq row[OtpTable.id] }) {
                it[used] = true
            }
            OtpValidationResult.Valid
        }
    }

    fun cleanupExpired() = transaction(db) {
        val cutoff = Clock.System.now().minus(24.hours)
        OtpTable.deleteWhere { OtpTable.createdAt less cutoff }
    }
}
