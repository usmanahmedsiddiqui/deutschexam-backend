package com.deutschexam.backend.plugins

import com.deutschexam.backend.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val code: String,
    val message: String,
    @SerialName("request_id")
    val requestId: String? = null,
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.PayloadTooLarge) { call, _ ->
            call.respond(
                HttpStatusCode.PayloadTooLarge,
                ApiErrorResponse(
                    code = "PAYLOAD_TOO_LARGE",
                    message = "Request body exceeds the maximum allowed size.",
                    requestId = call.callId,
                )
            )
        }
        status(HttpStatusCode.TooManyRequests) { call, _ ->
            call.response.headers.append(HttpHeaders.RetryAfter, "60")
            call.respond(
                HttpStatusCode.TooManyRequests,
                ApiErrorResponse(
                    code = "RATE_LIMITED",
                    message = "Too many requests. Please try again later.",
                    requestId = call.callId,
                )
            )
        }
        exception<ApiException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiErrorResponse(
                    code = cause.code,
                    message = cause.message ?: "An error occurred.",
                    requestId = call.callId,
                )
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorResponse(
                    code = "BAD_REQUEST",
                    message = cause.message ?: "Bad request.",
                    requestId = call.callId,
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorResponse(
                    code = "INTERNAL_ERROR",
                    message = "An unexpected error occurred.",
                    requestId = call.callId,
                )
            )
        }
    }
}
