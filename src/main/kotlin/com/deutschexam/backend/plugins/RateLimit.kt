package com.deutschexam.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

const val RATE_LIMIT_AUTH = "auth"
const val RATE_LIMIT_BUGS = "bugs"

fun Application.configureRateLimit() {
    install(RateLimit) {
        // 10 requests per minute per IP on auth endpoints
        register(RateLimitName(RATE_LIMIT_AUTH)) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
            requestKey { call -> call.request.local.remoteHost }
        }
        // 5 requests per minute per IP on bug reporting
        register(RateLimitName(RATE_LIMIT_BUGS)) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
            requestKey { call -> call.request.local.remoteHost }
        }
    }
}
