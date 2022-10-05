package com.horux.plugins

import com.horux.data.user.UserDataSource
import com.horux.routes.authenticate
import com.horux.routes.getSecretInfo
import com.horux.routes.signIn
import com.horux.routes.signUp
import com.horux.security.hashing.HashingService
import com.horux.security.token.TokenConfig
import com.horux.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenConfig: TokenConfig,
    tokenService: TokenService
) {

    routing {
        signIn(hashingService, userDataSource, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getSecretInfo()
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
