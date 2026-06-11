package com.deutschexam.backend.db.seed

/**
 * Orchestrates idempotent seeding of reference data. Each seeder is a no-op when its
 * table is already populated, so this is safe to run on every startup. Order matters:
 * later seeders reference rows created by earlier ones (FKs on level/provider).
 */
object DataSeeder {
    fun seedAll() {
        LevelSeeder.seed()
        ProviderSeeder.seed()
        ProductSeeder.seed()
        ExamDetailSeeder.seed()
        ExamSeeder.seed()
    }
}
