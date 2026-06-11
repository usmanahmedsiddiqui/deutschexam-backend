package com.deutschexam.backend.auth.routes

import com.deutschexam.backend.auth.model.GoogleAuthRequest
import com.deutschexam.backend.auth.model.RefreshTokenRequest
import com.deutschexam.backend.auth.service.AuthService
import com.deutschexam.backend.plugins.RATE_LIMIT_AUTH
import com.deutschexam.backend.util.ValidationException
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
                if (req.idToken.isBlank()) throw ValidationException("Google ID token is required.")
                val response = authService.googleSignIn(req)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/refresh") {
                val req = call.receive<RefreshTokenRequest>()
                if (req.refreshToken.isBlank()) throw ValidationException("Refresh token is required.")
                val response = authService.refresh(req)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}
