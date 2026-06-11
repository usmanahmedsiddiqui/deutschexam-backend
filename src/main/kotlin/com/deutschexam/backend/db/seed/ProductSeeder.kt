package com.deutschexam.backend.db.seed

import com.deutschexam.backend.db.tables.ProductsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

internal object ProductSeeder {
    fun seed() {
        if (!ProductsTable.selectAll().empty()) return

        for (product in loadSeedList<ProductSeed>("seed/products/products.json")) {
            ProductsTable.insert {
                it[id] = product.id
                it[levelId] = product.levelId
                it[priceCents] = product.priceCents
                it[discountedPriceCents] = product.discountedPriceCents
                it[currency] = product.currency
            }
        }
    }
}
