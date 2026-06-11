package com.deutschexam.backend.db.seed

import com.deutschexam.backend.db.tables.ExamDetailsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

internal object ExamDetailSeeder {
    private val files = listOf(
        "detail_telc_a1", "detail_telc_a2", "detail_telc_b1",
        "detail_goethe_a1", "detail_goethe_a2", "detail_goethe_b1",
    )

    fun seed() {
        if (!ExamDetailsTable.selectAll().empty()) return

        for (filename in files) {
            val text = loadSeedResource("seed/exam_detail/$filename.json") ?: continue
            val detail = seedJson.decodeFromString<ExamDetailSeed>(text)
            ExamDetailsTable.insert {
                it[id] = detail.id
                it[name] = detail.name
                it[providerId] = detail.providerId
                it[levelId] = detail.levelId
                it[totalPoints] = detail.totalPoints
                it[totalMinutes] = detail.totalMinutes
                it[data] = text
            }
        }
    }
}
