package com.diraj.kreddit.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RedditObject(
    val kind: String,
    val data: RedditObjectData
)