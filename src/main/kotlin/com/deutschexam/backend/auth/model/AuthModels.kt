package com.deutschexam.backend.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val gender: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class ConfirmEmailRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    @SerialName("newPassword") val newPassword: String,
)

@Serializable
data class LoginResponseDto(
    val token: String? = null,
    val name: String,
    val email: String,
    val gender: String,
    val emailConfirmed: Boolean,
    val ownedProductIds: List<String> = emptyList(),
)

@Serializable
data class MessageResponse(val message: String)

data class UserRecord(
    val id: String,
    val name: String,
    val email: String,
    val gender: String,
    val passwordHash: String,
    val emailConfirmed: Boolean,
    val ownedProductIds: List<String>,
)

enum class OtpType { EMAIL_CONFIRMATION, PASSWORD_RESET }

sealed class OtpValidationResult {
    object Valid : OtpValidationResult()
    object Invalid : OtpValidationResult()
    object Expired : OtpValidationResult()
}
