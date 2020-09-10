package com.diraj.kreddit.network.models

import com.google.gson.annotations.SerializedName

data class AccessTokenModel(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    val scope: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("refresh_token")
    val refreshToken: String ?= null
)