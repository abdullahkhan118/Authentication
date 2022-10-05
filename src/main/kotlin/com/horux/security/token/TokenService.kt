package com.horux.security.token

interface TokenService {

    fun generateToken(config: TokenConfig, vararg claims: TokenClaim): String

}