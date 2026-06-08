package com.deutschexam.backend.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(val idToken: String)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    @SerialName("token_expires_at") val tokenExpiresAt: Long,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_token_expires_at") val refreshTokenExpiresAt: Long,
    val name: String?,
    val email: String?,
    @SerialName("profile_picture") val profilePicture: String?,
    @SerialName("owned_product_ids") val ownedProductIds: List<String> = emptyList(),
)

data class UserRecord(
    val id: String,
    val name: String,
    val email: String,
    val profilePicture: String?,
    val ownedProductIds: List<String>,
)
