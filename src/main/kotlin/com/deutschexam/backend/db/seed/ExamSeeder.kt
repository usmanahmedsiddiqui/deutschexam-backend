package com.deutschexam.backend.db.seed

import com.deutschexam.backend.db.tables.ExamsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

internal object ExamSeeder {
    private val files = listOf(
        "exam_telc_a1_01", "exam_telc_a2_01", "exam_telc_b1_01",
        "exam_goethe_b1_01", "exam_goethe_a2_01",
    )

    fun seed() {
        if (!ExamsTable.selectAll().empty()) return

        for (filename in files) {
            val text = loadSeedResource("seed/$filename.json") ?: continue
            val exam = seedJson.decodeFromString<ExamSeed>(text)
            ExamsTable.insert {
                it[id] = exam.id
                it[examDetailId] = examDetailIdFromExamId(exam.id)
                it[levelId] = exam.level.id
                it[providerId] = exam.provider.id
                it[name] = exam.name
                it[isFree] = exam.isFree
                it[totalPoints] = exam.totalPoints
                it[totalMinutes] = exam.totalMinutes
                it[data] = text
            }
        }
    }

    /** telc_a1_01 -> telc_a1, goethe_b1_01 -> goethe_b1 */
    private fun examDetailIdFromExamId(examId: String): String =
        examId.split("_").dropLast(1).joinToString("_")
}
