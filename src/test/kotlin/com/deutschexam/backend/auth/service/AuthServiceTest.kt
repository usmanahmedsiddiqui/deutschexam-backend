package com.deutschexam.backend.auth.service

import com.deutschexam.backend.auth.model.RefreshTokenRequest
import com.deutschexam.backend.auth.model.UserRecord
import com.deutschexam.backend.auth.repository.RefreshTokenRepository
import com.deutschexam.backend.auth.repository.TokenLookup
import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.config.AppConfig
import com.deutschexam.backend.util.ApiErrorCode
import com.deutschexam.backend.util.JwtConfig
import com.deutschexam.backend.util.RefreshTokenExpiredException
import com.deutschexam.backend.util.RefreshTokenInvalidException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AuthServiceTest {

    private val userRepo = mockk<UserRepository>()
    private val refreshTokenRepo = mockk<RefreshTokenRepository>()
    private val service = AuthService(userRepo, refreshTokenRepo, googleClientId = "test-client-id")

    private val user = UserRecord(
        id = "user-1",
        name = "Test User",
        email = "test@example.com",
        profilePicture = null,
        ownedProductIds = listOf("p_a1"),
    )

    @BeforeTest
    fun setup() {
        JwtConfig.init(
            AppConfig(
                environment = "test",
                jwtSecret = "test-secret-that-is-long-enough-32chars",
                jwtIssuer = "test-issuer",
                jwtExpiryHours = 1,
                jwtRefreshTokenExpiryDays = 30,
                databaseUrl = "",
                databaseUser = "",
                databasePassword = "",
                databaseDriver = "",
                databasePoolSize = 1,
                googleClientId = "",
            )
        )
    }

    @Test
    fun `refresh with unknown token throws RefreshTokenInvalidException`() {
        every { refreshTokenRepo.findToken("bad-token") } returns null

        val ex = assertFailsWith<RefreshTokenInvalidException> {
            service.refresh(RefreshTokenRequest(refreshToken = "bad-token"))
        }
        assertEquals(ApiErrorCode.REFRESH_TOKEN_INVALID.name, ex.code)
    }

    @Test
    fun `refresh with expired token throws RefreshTokenExpiredException`() {
        every { refreshTokenRepo.findToken("expired-token") } returns TokenLookup(userId = "user-1", isExpired = true)

        val ex = assertFailsWith<RefreshTokenExpiredException> {
            service.refresh(RefreshTokenRequest(refreshToken = "expired-token"))
        }
        assertEquals(ApiErrorCode.REFRESH_TOKEN_EXPIRED.name, ex.code)
    }

    @Test
    fun `refresh with valid token but missing user throws RefreshTokenInvalidException`() {
        every { refreshTokenRepo.findToken("valid-token") } returns TokenLookup(userId = "user-1", isExpired = false)
        every { refreshTokenRepo.delete("valid-token") } returns 1
        every { userRepo.findById("user-1") } returns null

        assertFailsWith<RefreshTokenInvalidException> {
            service.refresh(RefreshTokenRequest(refreshToken = "valid-token"))
        }
    }

    @Test
    fun `refresh with valid token and existing user returns LoginResponseDto`() {
        every { refreshTokenRepo.findToken("valid-token") } returns TokenLookup(userId = "user-1", isExpired = false)
        every { refreshTokenRepo.delete("valid-token") } returns 1
        every { userRepo.findById("user-1") } returns user
        every { refreshTokenRepo.create("user-1") } returns Pair("new-refresh-token", 9999999L)

        val response = service.refresh(RefreshTokenRequest(refreshToken = "valid-token"))

        assertNotNull(response.token)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals("test@example.com", response.email)
        assertEquals("Test User", response.name)
        assertEquals(listOf("p_a1"), response.ownedProductIds)
    }

    @Test
    fun `refresh rotates the refresh token (old one is deleted)`() {
        every { refreshTokenRepo.findToken("old-token") } returns TokenLookup(userId = "user-1", isExpired = false)
        every { refreshTokenRepo.delete("old-token") } returns 1
        every { userRepo.findById("user-1") } returns user
        every { refreshTokenRepo.create("user-1") } returns Pair("new-token", 9999999L)

        service.refresh(RefreshTokenRequest(refreshToken = "old-token"))

        verify(exactly = 1) { refreshTokenRepo.delete("old-token") }
        verify(exactly = 1) { refreshTokenRepo.create("user-1") }
    }
}
