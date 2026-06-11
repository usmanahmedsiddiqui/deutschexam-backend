package com.deutschexam.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

/**
 * Request logging. The `Authorization` header value is never logged (SEC-9) — only the
 * method, path, and status are recorded by default, and we explicitly avoid logging headers
 * or bodies that could contain bearer / refresh tokens.
 */
fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        // Skip noisy health-check polling from uptime monitors.
        filter { call -> !call.request.path().startsWith("/health") }
        format { call ->
            val status = call.response.status()?.value ?: "-"
            val method = call.request.httpMethod.value
            val path = call.request.path()
            "$status $method $path"
        }
    }
}
