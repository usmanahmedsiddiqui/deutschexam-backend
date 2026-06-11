package com.deutschexam.backend.plugins

import com.deutschexam.backend.bugs.repository.BugRepository
import com.deutschexam.backend.bugs.routes.bugRoutes
import com.deutschexam.backend.auth.repository.RefreshTokenRepository
import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.auth.routes.authRoutes
import com.deutschexam.backend.auth.service.AuthService
import com.deutschexam.backend.exams.repository.ExamDetailRepository
import com.deutschexam.backend.exams.repository.ExamRepository
import com.deutschexam.backend.exams.routes.examRoutes
import com.deutschexam.backend.exams.service.ExamAccessService
import com.deutschexam.backend.levels.repository.LevelRepository
import com.deutschexam.backend.levels.routes.levelRoutes
import com.deutschexam.backend.providers.repository.ProviderRepository
import com.deutschexam.backend.providers.routes.providerRoutes
import com.deutschexam.backend.products.repository.ProductRepository
import com.deutschexam.backend.products.repository.UserProductRepository
import com.deutschexam.backend.products.routes.productRoutes
import com.deutschexam.backend.products.service.PurchaseService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(db: Database, googleClientId: String) {
    val userProductRepo = UserProductRepository(db)
    val userRepo = UserRepository(db, userProductRepo)
    val refreshTokenRepo = RefreshTokenRepository(db)
    val authService = AuthService(userRepo, refreshTokenRepo, googleClientId)
    val levelRepo = LevelRepository(db)
    val providerRepo = ProviderRepository(db)
    val productRepo = ProductRepository(db)
    val purchaseService = PurchaseService(productRepo, userProductRepo)
    val examDetailRepo = ExamDetailRepository(db)
    val examRepo = ExamRepository(db)
    val examAccessService = ExamAccessService(examRepo, productRepo, userProductRepo)
    val bugRepo = BugRepository(db)

    routing {
        staticResources("/static", "static")
        authRoutes(authService)
        levelRoutes(levelRepo)
        providerRoutes(providerRepo)
        productRoutes(productRepo, purchaseService)
        examRoutes(examDetailRepo, examRepo, examAccessService)
        bugRoutes(bugRepo)
    }
}
