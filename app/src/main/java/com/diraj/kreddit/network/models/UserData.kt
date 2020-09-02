package com.diraj.kreddit.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserData(
    val name: String,
    val icon_img: String,
    val created_utc: String
)