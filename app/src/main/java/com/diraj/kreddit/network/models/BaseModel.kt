package com.diraj.kreddit.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseModel(
    val kind: String,
    val data: ListingData
)
