package com.deutschexam.backend.db.seed

import com.deutschexam.backend.db.tables.ProviderLevelsTable
import com.deutschexam.backend.db.tables.ProvidersTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

internal object ProviderSeeder {
    fun seed() {
        if (!ProvidersTable.selectAll().empty()) return

        for (provider in loadSeedList<ProviderSeed>("seed/providers.json")) {
            ProvidersTable.insert {
                it[id] = provider.id
                it[name] = provider.name
                it[fullName] = provider.fullName
                it[description] = provider.description
                it[logo] = provider.logo
                it[website] = provider.website
            }
            for (levelId in provider.levelIds) {
                ProviderLevelsTable.insert {
                    it[providerId] = provider.id
                    it[ProviderLevelsTable.levelId] = levelId
                }
            }
        }
    }
}
