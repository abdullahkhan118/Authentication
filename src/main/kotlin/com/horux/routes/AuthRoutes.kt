package com.horux.routes

import com.horux.data.request.AuthRequest
import com.horux.data.response.AuthResponse
import com.horux.data.user.User
import com.horux.data.user.UserDataSource
import com.horux.security.hashing.HashingService
import com.horux.security.hashing.SHA256HashingService
import com.horux.security.hashing.SaltedHash
import com.horux.security.token.TokenClaim
import com.horux.security.token.TokenConfig
import com.horux.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("/signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(value = request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )

        if (!userDataSource.insertUser(user)) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK, "User with username ${request.username} has signup successfully")
    }
}

fun Route.signIn(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("/signin") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username) ?: kotlin.run {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = SaltedHash(hash = user.password, salt = user.salt)

        if (!hashingService.verifyHash(request.password, saltedHash)) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val token = tokenService.generateToken(tokenConfig, TokenClaim("userId", user.id.toString()))

        call.respond(HttpStatusCode.OK, AuthResponse(token))
    }
}

fun Route.authenticate(){
    authenticate {
        get("authenticate"){
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo(){
    authenticate {
        get("secret"){
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId",String::class)
            call.respond(HttpStatusCode.OK,"Your userID is $userId")
        }
    }
}