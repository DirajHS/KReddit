package com.diraj.kreddit.network.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class RedditObjectData (
    val author: String,
    val score: Int,
    val gilded: Int,
    val created_utc: Long,
    val permalink: String,
    val subreddit: String,
    val subreddit_name_prefixed: String,
    val title: String,
    val selftext: String,
    val url: String,
    val preview: RedditObjectPreview?,
    val thumbnail: String,
    val num_comments: Int,
    val over_18: Boolean,
    val pinned: Boolean,
    val body: String?,
    val depth: Int?,
    val ups: Int
) : Parcelable