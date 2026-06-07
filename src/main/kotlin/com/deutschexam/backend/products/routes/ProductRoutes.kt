package com.deutschexam.backend.products.routes

import com.deutschexam.backend.plugins.UserPrincipal
import com.deutschexam.backend.products.repository.ProductRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productRepo: ProductRepository) {
    route("/products") {
        get {
            call.respond(HttpStatusCode.OK, productRepo.getAllProducts())
        }

        authenticate("jwt-auth") {
            post("/{id}/buy") {
                val principal = call.principal<UserPrincipal>()!!
                val productId = call.parameters["id"]!!
                val result = productRepo.buyProduct(productId, principal.userId)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
