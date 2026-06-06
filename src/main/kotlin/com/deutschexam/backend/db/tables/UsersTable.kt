package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val googleId = varchar("google_id", 255).uniqueIndex()
    val name = varchar("name", 100)
    val email = varchar("email", 255).uniqueIndex()
    val pictureUrl = varchar("picture_url", 500).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
