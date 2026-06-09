package com.deutschexam.backend.bugs.repository

import com.deutschexam.backend.bugs.model.BugReportResponse
import com.deutschexam.backend.db.tables.BugsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class BugRepository(private val db: Database) {

    fun create(title: String, description: String): BugReportResponse = transaction(db) {
        val row = BugsTable.insert {
            it[BugsTable.title] = title
            it[BugsTable.description] = description
        }
        BugReportResponse(
            id = row[BugsTable.id].toString(),
            title = row[BugsTable.title],
            description = row[BugsTable.description],
        )
    }
}
