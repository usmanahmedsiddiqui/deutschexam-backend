package com.deutschexam.backend.auth.service

import com.deutschexam.backend.auth.model.*
import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.util.*

class AuthService(
    private val userRepo: UserRepository,
    private val otpService: OtpService,
    private val emailService: EmailService,
) {
    fun register(req: RegisterRequest): LoginResponseDto {
        if (req.name.isBlank()) throw ValidationException("name", "Name is required.")
        if (!req.email.contains("@")) throw ValidationException("email", "Valid email is required.")
        if (req.password.length < 6) throw ValidationException("password", "Password must be at least 6 characters.")
        if (req.gender !in listOf("Male", "Female", "Other")) throw ValidationException("gender", "Gender must be Male, Female, or Other.")
        if (userRepo.existsByEmail(req.email)) throw ConflictException("Email already registered.")

        val hash = PasswordUtils.hash(req.password)
        val user = userRepo.create(req.name, req.gender, req.email, hash)
        val otp = otpService.generate(req.email, OtpType.EMAIL_CONFIRMATION)
        emailService.sendConfirmationEmail(req.email, req.name, otp)

        return user.toResponseDto(token = null)
    }

    fun login(req: LoginRequest): LoginResponseDto {
        val user = userRepo.findByEmail(req.email)
        if (user == null || !PasswordUtils.verify(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password.")
        }
        val token = if (user.emailConfirmed) JwtConfig.generateToken(user.id, user.email) else null
        return user.toResponseDto(token)
    }

    fun confirmEmail(req: ConfirmEmailRequest): LoginResponseDto {
        when (otpService.validate(req.email, req.otp, OtpType.EMAIL_CONFIRMATION)) {
            is OtpValidationResult.Invalid -> throw IllegalArgumentException("Invalid code. Please try again.")
            is OtpValidationResult.Expired -> throw IllegalArgumentException("Code has expired. Please request a new one.")
            is OtpValidationResult.Valid -> Unit
        }
        userRepo.setEmailConfirmed(req.email)
        val user = userRepo.findByEmail(req.email) ?: throw NotFoundException("User not found.")
        val token = JwtConfig.generateToken(user.id, user.email)
        return user.toResponseDto(token)
    }

    fun forgotPassword(req: ForgotPasswordRequest) {
        val user = userRepo.findByEmail(req.email) ?: return
        val otp = otpService.generate(req.email, OtpType.PASSWORD_RESET)
        emailService.sendPasswordResetEmail(req.email, otp)
    }

    fun resetPassword(req: ResetPasswordRequest) {
        if (req.newPassword.length < 6) throw ValidationException("newPassword", "Password must be at least 6 characters.")
        when (otpService.validate(req.email, req.otp, OtpType.PASSWORD_RESET)) {
            is OtpValidationResult.Invalid -> throw IllegalArgumentException("Invalid code. Please try again.")
            is OtpValidationResult.Expired -> throw IllegalArgumentException("Code has expired. Please request a new one.")
            is OtpValidationResult.Valid -> Unit
        }
        userRepo.updatePassword(req.email, PasswordUtils.hash(req.newPassword))
    }

    private fun UserRecord.toResponseDto(token: String?) = LoginResponseDto(
        token = token,
        name = name,
        email = email,
        gender = gender,
        emailConfirmed = emailConfirmed,
        ownedProductIds = ownedProductIds,
    )
}
