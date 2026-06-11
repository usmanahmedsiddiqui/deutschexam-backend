package com.deutschexam.backend.config

import io.ktor.server.config.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppConfigTest {

    private fun config(vararg pairs: Pair<String, String>): ApplicationConfig {
        val map = mapOf(*pairs)
        return MapApplicationConfig(*map.entries.map { it.key to it.value }.toTypedArray())
    }

    private fun validDevConfig(overrides: Map<String, String> = emptyMap()): ApplicationConfig {
        val base = mapOf(
            "app.environment" to "dev",
            "jwt.secret" to "dev-secret-change-in-production-min-32-chars",
            "jwt.issuer" to "deutschexam",
            "jwt.expiry_hours" to "24",
            "database.url" to "jdbc:postgresql://localhost/test",
            "database.user" to "user",
            "database.password" to "pass",
            "database.driver" to "org.postgresql.Driver",
            "database.pool_size" to "5",
        ) + overrides
        return MapApplicationConfig(*base.entries.map { it.key to it.value }.toTypedArray())
    }

    @Test
    fun `valid dev config loads without error`() {
        val appConfig = AppConfig.from(validDevConfig())
        assertEquals("dev", appConfig.environment)
    }

    @Test
    fun `JWT secret shorter than 32 chars fails in any environment`() {
        val ex = assertFailsWith<IllegalStateException> {
            AppConfig.from(validDevConfig(mapOf("jwt.secret" to "short")))
        }
        assertTrue(ex.message!!.contains("JWT_SECRET must be at least"))
    }

    @Test
    fun `prod env with dev default JWT secret fails`() {
        val ex = assertFailsWith<IllegalStateException> {
            AppConfig.from(validDevConfig(mapOf(
                "app.environment" to "prod",
                "jwt.secret" to "dev-secret-change-in-production-min-32-chars",
                "cors.allowed_origins" to "https://app.example.com",
                "google.client_id" to "google-client-id",
            )))
        }
        assertTrue(ex.message!!.contains("insecure dev default"))
    }

    @Test
    fun `prod env with missing google client id fails`() {
        val ex = assertFailsWith<IllegalStateException> {
            AppConfig.from(validDevConfig(mapOf(
                "app.environment" to "prod",
                "jwt.secret" to "a-very-secure-production-secret-abc123",
                "cors.allowed_origins" to "https://app.example.com",
            )))
        }
        assertTrue(ex.message!!.contains("GOOGLE_CLIENT_ID"))
    }

    @Test
    fun `prod env with missing CORS origins fails`() {
        val ex = assertFailsWith<IllegalStateException> {
            AppConfig.from(validDevConfig(mapOf(
                "app.environment" to "prod",
                "jwt.secret" to "a-very-secure-production-secret-abc123",
                "google.client_id" to "google-client-id",
            )))
        }
        assertTrue(ex.message!!.contains("CORS_ALLOWED_ORIGINS"))
    }

    @Test
    fun `prod env with all required fields loads without error`() {
        val appConfig = AppConfig.from(validDevConfig(mapOf(
            "app.environment" to "prod",
            "jwt.secret" to "a-very-secure-production-secret-abc123",
            "google.client_id" to "google-client-id",
            "cors.allowed_origins" to "https://app.example.com",
        )))
        assertTrue(appConfig.isProd)
    }

    @Test
    fun `CORS origins are split and trimmed correctly`() {
        val appConfig = AppConfig.from(validDevConfig(mapOf(
            "cors.allowed_origins" to "https://app.example.com , https://admin.example.com",
        )))
        assertEquals(listOf("https://app.example.com", "https://admin.example.com"), appConfig.corsAllowedOrigins)
    }

    @Test
    fun `dev env is lenient about missing prod-only fields`() {
        // Dev does not require google client id or cors origins
        val appConfig = AppConfig.from(validDevConfig())
        assertEquals("dev", appConfig.environment)
    }
}
