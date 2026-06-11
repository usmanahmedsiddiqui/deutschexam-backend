package com.deutschexam.backend.plugins

import com.deutschexam.backend.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS(config: AppConfig) {
    install(CORS) {
        if (config.isProd) {
            // Prod: only the configured origins (validated non-empty in AppConfig).
            config.corsAllowedOrigins.forEach { origin ->
                val scheme = origin.substringBefore("://", "https")
                val host = origin.substringAfter("://")
                allowHost(host, schemes = listOf(scheme))
            }
        } else {
            // Local dev convenience only.
            anyHost()
        }
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }
}
