package com.deutschexam.backend.db.seed

import com.deutschexam.backend.db.tables.LevelCatalogTable
import com.deutschexam.backend.db.tables.LevelsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

internal object LevelSeeder {
    fun seed() {
        if (!LevelsTable.selectAll().empty()) return

        for (level in loadSeedList<LevelSeed>("seed/levels.json")) {
            LevelsTable.insert {
                it[id] = level.id
                it[name] = level.name
                it[description] = level.description
                it[shortDescription] = level.shortDescription
            }
            for (entry in level.catalog) {
                LevelCatalogTable.insert {
                    it[levelId] = level.id
                    it[type] = entry.type
                    it[isFree] = entry.isFree
                }
            }
        }
    }
}
