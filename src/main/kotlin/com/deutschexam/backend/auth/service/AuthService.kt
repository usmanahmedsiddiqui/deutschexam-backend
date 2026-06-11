package com.deutschexam.backend.auth.service

import com.deutschexam.backend.auth.model.GoogleAuthRequest
import com.deutschexam.backend.auth.model.LoginResponseDto
import com.deutschexam.backend.auth.model.RefreshTokenRequest
import com.deutschexam.backend.auth.repository.RefreshTokenRepository
import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.util.GoogleTokenInvalidException
import com.deutschexam.backend.util.JwtConfig
import com.deutschexam.backend.util.RefreshTokenExpiredException
import com.deutschexam.backend.util.RefreshTokenInvalidException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory

class AuthService(
    private val userRepo: UserRepository,
    private val refreshTokenRepo: RefreshTokenRepository,
    googleClientId: String,
) {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(listOf(googleClientId))
        .build()

    fun googleSignIn(req: GoogleAuthRequest): LoginResponseDto {
        val idToken = verifier.verify(req.idToken)
            ?: throw GoogleTokenInvalidException()

        val payload = idToken.payload
        val googleId = payload.subject
        val email = payload.email ?: throw GoogleTokenInvalidException()
        val name = payload["name"] as? String ?: email
        val profilePicture = payload["picture"] as? String

        val user = userRepo.findOrCreate(googleId, email, name, profilePicture)
        return buildLoginResponse(user.id, user.email, user.name, user.profilePicture, user.ownedProductIds)
    }

    fun refresh(req: RefreshTokenRequest): LoginResponseDto {
        val lookup = refreshTokenRepo.findToken(req.refreshToken)
            ?: throw RefreshTokenInvalidException()

        if (lookup.isExpired) throw RefreshTokenExpiredException()

        refreshTokenRepo.delete(req.refreshToken)

        val user = userRepo.findById(lookup.userId)
            ?: throw RefreshTokenInvalidException()

        return buildLoginResponse(user.id, user.email, user.name, user.profilePicture, user.ownedProductIds)
    }

    private fun buildLoginResponse(
        userId: String,
        email: String,
        name: String,
        profilePicture: String?,
        ownedProductIds: List<String>
    ): LoginResponseDto {
        val token = JwtConfig.generateToken(userId, email)
        val tokenExpiresAt = JwtConfig.tokenExpiresAt()
        val (refreshToken, refreshTokenExpiresAt) = refreshTokenRepo.create(userId)

        return LoginResponseDto(
            token = token,
            tokenExpiresAt = tokenExpiresAt,
            refreshToken = refreshToken,
            refreshTokenExpiresAt = refreshTokenExpiresAt,
            name = name,
            email = email,
            profilePicture = profilePicture,
            ownedProductIds = ownedProductIds,
        )
    }
}
