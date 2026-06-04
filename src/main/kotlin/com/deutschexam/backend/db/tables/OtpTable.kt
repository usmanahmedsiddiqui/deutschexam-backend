package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OtpTable : Table("otps") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255)
    val code = varchar("code", 4)
    val type = varchar("type", 30) // EMAIL_CONFIRMATION | PASSWORD_RESET
    val expiresAt = timestamp("expires_at")
    val used = bool("used").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, email, type)
    }
}
