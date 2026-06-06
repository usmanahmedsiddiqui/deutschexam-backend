package com.deutschexam.backend.auth.service

import com.deutschexam.backend.auth.model.GoogleAuthRequest
import com.deutschexam.backend.auth.model.LoginResponseDto
import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.util.AuthException
import com.deutschexam.backend.util.JwtConfig
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory

class AuthService(
    private val userRepo: UserRepository,
    googleClientId: String,
) {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(listOf(googleClientId))
        .build()

    fun googleSignIn(req: GoogleAuthRequest): LoginResponseDto {
        val idToken = verifier.verify(req.idToken)
            ?: throw AuthException("Invalid Google ID token.")

        val payload = idToken.payload
        val googleId = payload.subject
        val email = payload.email ?: throw AuthException("Google account has no email.")
        val name = payload["name"] as? String ?: email
        val profilePicture = payload["picture"] as? String

        val user = userRepo.findOrCreate(googleId, email, name, profilePicture)
        val token = JwtConfig.generateToken(user.id, user.email)

        return LoginResponseDto(
            token = token,
            name = user.name,
            email = user.email,
            phoneNumber = null,
            profilePicture = user.profilePicture,
            ownedProductIds = user.ownedProductIds,
        )
    }
}
