package com.diraj.kreddit.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val name: String,
    @SerialName("icon_img")
    val iconImg: String,
    @SerialName("created_utc")
    val createdUtc: String
)