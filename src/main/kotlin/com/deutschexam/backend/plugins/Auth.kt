package com.deutschexam.backend.plugins

import com.auth0.jwt.JWT
import com.deutschexam.backend.util.ApiErrorCode
import com.deutschexam.backend.util.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.Date

data class UserPrincipal(val userId: String, val email: String) : Principal

fun Application.configureAuth() {
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.subject ?: return@validate null
                val email = credential.payload.getClaim(JwtConfig.CLAIM_EMAIL).asString() ?: return@validate null
                UserPrincipal(userId, email)
            }
            challenge { _, _ ->
                val authHeader = call.request.header(HttpHeaders.Authorization)
                val (code, message) = when {
                    authHeader == null ->
                        ApiErrorCode.TOKEN_MISSING.name to "Authentication is required to access this resource."
                    else -> {
                        val expired = try {
                            val token = authHeader.removePrefix("Bearer ").trim()
                            val decoded = JWT.decode(token)
                            decoded.expiresAt != null && decoded.expiresAt.before(Date())
                        } catch (_: Exception) {
                            false
                        }
                        if (expired) {
                            ApiErrorCode.TOKEN_EXPIRED.name to "Access token has expired."
                        } else {
                            ApiErrorCode.TOKEN_INVALID.name to "Access token is invalid."
                        }
                    }
                }
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrorResponse(code = code, message = message, requestId = call.callId),
                )
            }
        }
    }
}
