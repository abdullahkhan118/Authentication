package com.horux

import com.horux.data.user.MongoUserDataSource
import io.ktor.server.application.*
import com.horux.plugins.*
import com.horux.security.hashing.SHA256HashingService
import com.horux.security.token.JwtTokenService
import com.horux.security.token.TokenConfig
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val password = System.getenv("MONGO_PWD")
    val dbName = "login_credentials"
    val db =
        KMongo
            .createClient("mongodb+srv://abdullahkhan:$password@cluster0.pua4y.mongodb.net/$dbName?retryWrites=true&w=majority")
            .coroutine
            .getDatabase(dbName)

    val dataSource = MongoUserDataSource(db)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").toString(),
        audience = environment.config.property("jwt.audience").toString(),
        expiresIn = TimeUnit.MILLISECONDS.convert(365L,TimeUnit.DAYS),
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSecurity(config = tokenConfig)
    configureSerialization()
    configureMonitoring()
    configureRouting(hashingService, dataSource,tokenConfig, tokenService)
}
