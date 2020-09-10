package com.diraj.kreddit.network.models

import com.google.gson.annotations.SerializedName

data class UserData(
    val name: String,
    @SerializedName("icon_img")
    val iconImg: String,
    @SerializedName("created_utc")
    val createdUtc: String
)