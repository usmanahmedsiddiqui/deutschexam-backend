package com.deutschexam.backend.auth.service

import io.ktor.server.config.*
import jakarta.mail.*
import jakarta.mail.internet.*
import org.slf4j.LoggerFactory
import java.util.*

interface EmailService {
    fun sendConfirmationEmail(to: String, name: String, otp: String)
    fun sendPasswordResetEmail(to: String, otp: String)
}

class SmtpEmailService(config: ApplicationConfig) : EmailService {

    private val log = LoggerFactory.getLogger(SmtpEmailService::class.java)

    private val host = config.property("email.host").getString()
    private val port = config.property("email.port").getString().toInt()
    private val username = config.property("email.username").getString()
    private val password = config.property("email.password").getString()
    private val from = config.property("email.from").getString()
    private val useTls = config.property("email.use_tls").getString().toBoolean()

    private val session: Session by lazy {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            if (username.isNotBlank()) {
                put("mail.smtp.auth", "true")
                if (useTls) {
                    put("mail.smtp.starttls.enable", "true")
                }
            }
        }
        if (username.isNotBlank()) {
            Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
            })
        } else {
            Session.getInstance(props)
        }
    }

    override fun sendConfirmationEmail(to: String, name: String, otp: String) {
        val subject = "Your DeutschExam confirmation code: $otp"
        val body = """
            Hi $name,

            Your email confirmation code is:

                $otp

            This code expires in 15 minutes.

            If you did not create a DeutschExam account, you can ignore this email.
        """.trimIndent()
        sendEmail(to, subject, body)
    }

    override fun sendPasswordResetEmail(to: String, otp: String) {
        val subject = "Your DeutschExam password reset code: $otp"
        val body = """
            Hi,

            You requested a password reset. Your code is:

                $otp

            This code expires in 15 minutes.

            If you did not request a password reset, you can ignore this email.
        """.trimIndent()
        sendEmail(to, subject, body)
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        try {
            MimeMessage(session).apply {
                setFrom(InternetAddress(from, "DeutschExam"))
                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject)
                setText(body)
            }.let { Transport.send(it) }
        } catch (e: Exception) {
            log.error("Failed to send email to $to: ${e.message}", e)
            throw e
        }
    }
}

class ConsoleEmailService : EmailService {
    private val log = LoggerFactory.getLogger(ConsoleEmailService::class.java)

    override fun sendConfirmationEmail(to: String, name: String, otp: String) {
        log.info("=== EMAIL CONFIRMATION ===  To: $to  OTP: $otp  ===")
    }

    override fun sendPasswordResetEmail(to: String, otp: String) {
        log.info("=== PASSWORD RESET ===  To: $to  OTP: $otp  ===")
    }
}
