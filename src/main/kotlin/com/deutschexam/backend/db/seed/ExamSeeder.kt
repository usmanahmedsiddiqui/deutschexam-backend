package com.deutschexam.backend.db.seed

import com.deutschexam.backend.db.model.ExamSeed
import com.deutschexam.backend.db.tables.ExamsTable
import org.jetbrains.exposed.sql.upsert

internal object ExamSeeder {
    private const val INDEX = "seed/exams/index.txt"  // paths inside are relative to seed/exams/

    @Suppress("DuplicatedCode")
    fun seed() {
        val paths = examPaths()
        for (path in paths) {
            val text = loadSeedResource("seed/exams/$path") ?: continue
            val exam = seedJson.decodeFromString<ExamSeed>(text)
            ExamsTable.upsert(ExamsTable.id) {
                it[id] = exam.id
                it[examDetailId] = examDetailIdFromExamId(exam.id)
                it[levelId] = exam.levelId
                it[providerId] = exam.providerId
                it[name] = exam.name
                it[isFree] = exam.isFree
                it[totalPoints] = exam.totalPoints
                it[totalMinutes] = exam.totalMinutes
                it[data] = text
            }
        }
    }

    private fun examPaths(): List<String> =
        loadSeedResource(INDEX)
            ?.lines()
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }

    /** telc_a1_01 -> telc_a1, goethe_b1_01 -> goethe_b1 */
    private fun examDetailIdFromExamId(examId: String): String =
        examId.split("_").dropLast(1).joinToString("_")
}
