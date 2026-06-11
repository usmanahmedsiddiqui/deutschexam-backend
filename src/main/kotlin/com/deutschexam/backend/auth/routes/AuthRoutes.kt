package com.deutschexam.backend.auth.routes

import com.deutschexam.backend.auth.model.GoogleAuthRequest
import com.deutschexam.backend.auth.model.RefreshTokenRequest
import com.deutschexam.backend.auth.service.AuthService
import com.deutschexam.backend.plugins.RATE_LIMIT_AUTH
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    rateLimit(RateLimitName(RATE_LIMIT_AUTH)) {
        route("/auth") {
            post("/google") {
                val req = call.receive<GoogleAuthRequest>()
                val response = authService.googleSignIn(req)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/refresh") {
                val req = call.receive<RefreshTokenRequest>()
                val response = authService.refresh(req)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
