package com.deutschexam.backend.util

import io.ktor.http.*

sealed class ApiException(
    val code: String,
    message: String,
    val statusCode: HttpStatusCode,
) : Exception(message)

// ── Auth / 401 ────────────────────────────────────────────────────────────────

class AuthException(message: String = "Unauthorized", code: String = "UNAUTHORIZED") :
    ApiException(code, message, HttpStatusCode.Unauthorized)

class TokenMissingException :
    ApiException("TOKEN_MISSING", "Authentication is required to access this resource.", HttpStatusCode.Unauthorized)

class TokenInvalidException :
    ApiException("TOKEN_INVALID", "Access token is invalid.", HttpStatusCode.Unauthorized)

class TokenExpiredException :
    ApiException("TOKEN_EXPIRED", "Access token has expired.", HttpStatusCode.Unauthorized)

class RefreshTokenInvalidException :
    ApiException("REFRESH_TOKEN_INVALID", "Refresh token is invalid or has been revoked.", HttpStatusCode.Unauthorized)

class RefreshTokenExpiredException :
    ApiException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired. Please sign in again.", HttpStatusCode.Unauthorized)

class GoogleTokenInvalidException :
    ApiException("GOOGLE_TOKEN_INVALID", "Google sign-in failed. Please try again.", HttpStatusCode.Unauthorized)

// ── Forbidden / 403 ───────────────────────────────────────────────────────────

class ForbiddenException(message: String = "Forbidden", code: String = "FORBIDDEN") :
    ApiException(code, message, HttpStatusCode.Forbidden)

// ── Resource / 404 ────────────────────────────────────────────────────────────

class NotFoundException(message: String = "Not found", code: String = "NOT_FOUND") :
    ApiException(code, message, HttpStatusCode.NotFound)

// ── Conflict / 409 ────────────────────────────────────────────────────────────

class ConflictException(message: String, code: String = "CONFLICT") :
    ApiException(code, message, HttpStatusCode.Conflict)

// ── Validation / 422 ─────────────────────────────────────────────────────────

class ValidationException(message: String) :
    ApiException("VALIDATION_ERROR", message, HttpStatusCode.UnprocessableEntity)
