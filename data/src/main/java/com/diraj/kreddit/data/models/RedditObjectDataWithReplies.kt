package com.diraj.kreddit.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.diraj.kreddit.data.db.typeconverters.BaseModelConverter
import com.diraj.kreddit.data.db.typeconverters.RedditObjectConverter
import com.diraj.kreddit.data.db.typeconverters.RedditObjectPreviewConverter
import com.diraj.kreddit.data.db.typeconverters.ResolutionConverters
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Entity
@TypeConverters(value = [RedditObjectPreviewConverter::class, BaseModelConverter::class,
    RedditObjectConverter::class,
    ResolutionConverters::class])
@Serializable
data class RedditObjectDataWithReplies (
    val author: String? = null,
    val score: Int?= null,
    @SerialName("created_utc")
    val createdUtc: Double?= null,
    val permalink: String?= null,
    val subReddit: String?= null,
    @SerialName("subreddit_name_prefixed")
    val subredditNamePrefixed: String?= null,
    val title: String?= null,
    val url: String?= null,
    @SerialName("url_overridden_by_dest")
    val urlOverriddenByDest: String? = null,
    val preview: RedditObjectPreview?= null,
    val thumbnail: String?= null,
    @SerialName("num_comments")
    val numComments: Int?= null,
    val body: String?= null,
    val ups: Int?= null,
    val likes: Boolean? = null,
    val replies: BaseModel ?= null,
    val id: String?= null,
    @SerialName("selftext_html")
    val selfTextHtml: String? = null,
    val indexInResponse: Int = -1,
    @PrimaryKey var name: String
) : Parcelable
