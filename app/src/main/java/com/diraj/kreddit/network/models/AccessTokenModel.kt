package com.diraj.kreddit.network.models

data class AccessTokenModel(
    val access_token: String,
    val expires_in: Int,
    val scope: String,
    val token_type: String,
    val refresh_token: String ?= null
)