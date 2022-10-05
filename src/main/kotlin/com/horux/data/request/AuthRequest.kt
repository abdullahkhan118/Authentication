package com.horux.data.request

@kotlinx.serialization.Serializable
data class AuthRequest(
    val username: String,
    val password: String
)
