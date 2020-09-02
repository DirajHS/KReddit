package com.diraj.kreddit.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ListingData(
    val after: String,
    val before: String?,
    val children: List<RedditObject>
)