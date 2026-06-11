package com.deutschexam.backend.config

import io.ktor.server.config.*

/**
 * Typed application configuration with fail-fast validation.
 *
 * In the `prod` environment, missing or insecure secrets cause startup to abort with a clear
 * message rather than silently falling back to insecure dev defaults (SEC-3).
 */
data class AppConfig(
    val environment: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtExpiryHours: Long,
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val databaseDriver: String,
    val databasePoolSize: Int,
    val googleClientId: String,
    val corsAllowedOrigins: List<String>,
) {
    val isProd: Boolean get() = environment.equals("prod", ignoreCase = true)

    companion object {
        // Values that must never reach production.
        private const val DEV_JWT_SECRET = "dev-secret-change-in-production-min-32-chars"
        private const val MIN_JWT_SECRET_LENGTH = 32

        fun from(config: ApplicationConfig): AppConfig {
            val environment = config.propertyOrNull("app.environment")?.getString() ?: "dev"

            val appConfig = AppConfig(
                environment = environment,
                jwtSecret = config.property("jwt.secret").getString(),
                jwtIssuer = config.property("jwt.issuer").getString(),
                jwtExpiryHours = config.property("jwt.expiry_hours").getString().toLong(),
                databaseUrl = config.property("database.url").getString(),
                databaseUser = config.property("database.user").getString(),
                databasePassword = config.property("database.password").getString(),
                databaseDriver = config.property("database.driver").getString(),
                databasePoolSize = config.property("database.pool_size").getString().toInt(),
                googleClientId = config.propertyOrNull("google.client_id")?.getString().orEmpty(),
                corsAllowedOrigins = config.propertyOrNull("cors.allowed_origins")?.getString()
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList(),
            )

            appConfig.validate()
            return appConfig
        }
    }

    private fun validate() {
        val errors = mutableListOf<String>()

        if (jwtSecret.length < MIN_JWT_SECRET_LENGTH) {
            errors += "JWT_SECRET must be at least $MIN_JWT_SECRET_LENGTH characters."
        }

        if (isProd) {
            if (jwtSecret == DEV_JWT_SECRET) {
                errors += "JWT_SECRET is still set to the insecure dev default."
            }
            if (databaseUrl.isBlank()) errors += "DATABASE_URL is required."
            if (databaseUser.isBlank()) errors += "DATABASE_USER is required."
            if (databasePassword.isBlank()) errors += "DATABASE_PASSWORD is required."
            if (googleClientId.isBlank()) errors += "GOOGLE_CLIENT_ID is required."
            if (corsAllowedOrigins.isEmpty()) errors += "CORS_ALLOWED_ORIGINS is required."
        }

        if (errors.isNotEmpty()) {
            throw IllegalStateException(
                "Invalid configuration for environment '$environment':\n" +
                    errors.joinToString("\n") { "  - $it" },
            )
        }
    }
}
