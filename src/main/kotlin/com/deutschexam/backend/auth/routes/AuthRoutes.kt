package com.deutschexam.backend.auth.routes

import com.deutschexam.backend.auth.model.*
import com.deutschexam.backend.auth.service.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val response = authService.register(req)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val response = authService.login(req)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/confirm-email") {
            val req = call.receive<ConfirmEmailRequest>()
            val response = authService.confirmEmail(req)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/forgot-password") {
            val req = call.receive<ForgotPasswordRequest>()
            authService.forgotPassword(req)
            call.respond(HttpStatusCode.OK, MessageResponse("If that email is registered, a code has been sent."))
        }

        post("/reset-password") {
            val req = call.receive<ResetPasswordRequest>()
            authService.resetPassword(req)
            call.respond(HttpStatusCode.OK, MessageResponse("Password updated successfully."))
        }
    }
}
