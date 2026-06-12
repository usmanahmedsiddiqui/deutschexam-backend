package com.deutschexam.backend.util

import io.ktor.http.*

sealed class ApiException(
    val code: String,
    message: String,
    val statusCode: HttpStatusCode,
) : Exception(message)

class TokenMissingException :
    ApiException(ApiErrorCode.TOKEN_MISSING.name, "Authentication is required to access this resource.", HttpStatusCode.Unauthorized)

class RefreshTokenInvalidException :
    ApiException(ApiErrorCode.REFRESH_TOKEN_INVALID.name, "Refresh token is invalid or has been revoked.", HttpStatusCode.Unauthorized)

class RefreshTokenExpiredException :
    ApiException(ApiErrorCode.REFRESH_TOKEN_EXPIRED.name, "Refresh token has expired. Please sign in again.", HttpStatusCode.Unauthorized)

class GoogleTokenInvalidException :
    ApiException(ApiErrorCode.GOOGLE_TOKEN_INVALID.name, "Google sign-in failed. Please try again.", HttpStatusCode.BadRequest)

class ForbiddenException(code: ApiErrorCode, message: String) :
    ApiException(code.name, message, HttpStatusCode.Forbidden)

class NotFoundException(code: ApiErrorCode, message: String) :
    ApiException(code.name, message, HttpStatusCode.NotFound)

class ConflictException(code: ApiErrorCode, message: String) :
    ApiException(code.name, message, HttpStatusCode.Conflict)

class ValidationException(message: String) :
    ApiException(ApiErrorCode.VALIDATION_ERROR.name, message, HttpStatusCode.UnprocessableEntity)

enum class ApiErrorCode {
    TOKEN_MISSING,
    REFRESH_TOKEN_INVALID,
    REFRESH_TOKEN_EXPIRED,
    GOOGLE_TOKEN_INVALID,
    EXAM_NOT_OWNED,
    EXAM_DETAIL_NOT_FOUND,
    EXAM_NOT_FOUND,
    PRODUCT_NOT_FOUND,
    PRODUCT_ALREADY_OWNED,
    VALIDATION_ERROR
}