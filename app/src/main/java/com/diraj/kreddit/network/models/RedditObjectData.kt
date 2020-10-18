package com.diraj.kreddit.network.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.diraj.kreddit.network.models.post.VoteModel
import com.diraj.kreddit.presentation.home.db.typeconverters.BaseModelConverter
import com.diraj.kreddit.presentation.home.db.typeconverters.RedditObjectConverter
import com.diraj.kreddit.presentation.home.db.typeconverters.RedditObjectPreviewConverter
import com.diraj.kreddit.presentation.home.db.typeconverters.ResolutionConverters
import com.diraj.kreddit.utils.KRedditConstants
import com.diraj.kreddit.utils.RedditObjectDataSerializer
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = RedditObjectDataSerializer::class)
sealed class RedditObjectData: Parcelable {

    @Parcelize
    @Entity
    @TypeConverters(value = [RedditObjectPreviewConverter::class, BaseModelConverter::class,
        RedditObjectConverter::class,
        ResolutionConverters::class])
    @kotlinx.serialization.Serializable
    data class RedditObjectDataWithoutReplies (
        var author: String? = null,
        var score: Int?= null,
        @SerialName("created_utc")
        var createdUtc: Double?= null,
        var permalink: String?= null,
        var subReddit: String?= null,
        @SerialName("subreddit_name_prefixed")
        var subredditNamePrefixed: String?= null,
        var title: String?= null,
        var url: String?= null,
        @SerialName("url_overridden_by_dest")
        var urlOverriddenByDest: String? = null,
        var preview: RedditObjectPreview?= null,
        var thumbnail: String?= null,
        @SerialName("num_comments")
        var numComments: Int?= null,
        var body: String?= null,
        var ups: Int?= null,
        var likes: Boolean? = null,
        var replies: String ?= null,
        var id: String?= null,
        @SerialName("selftext_html")
        var selfTextHtml: String? = null,
        var indexInResponse: Int = -1,
        @PrimaryKey var name: String
    ) : Parcelable, RedditObjectData() {

        fun getDomain(): String? {
            return try {
                val host = Uri.parse(url).host
                if (host?.startsWith("www.") == true) host.substring(4) else host
            } catch (ex: Exception) {
                null
            }
        }

        fun getVoteModelToPost(clickedBtnType: String): VoteModel? {
            return when(clickedBtnType) {
                KRedditConstants.CLICKED_LIKE -> {
                    when (likes) {
                        true -> {
                            ups = ups?.minus(1)
                            likes = null
                            VoteModel(name, "0")
                        }
                        false -> {
                            ups = ups?.plus(2)
                            likes = true
                            VoteModel(name, "1")
                        }
                        else -> {
                            ups = ups?.plus(1)
                            likes = true
                            VoteModel(name, "1")
                        }
                    }
                }
                KRedditConstants.CLICKED_DISLIKE -> {
                    when (likes) {
                        false -> {
                            ups = ups?.plus(1)
                            likes = null
                            VoteModel(name, "0")
                        }
                        true -> {
                            ups = ups?.minus(2)
                            likes = false
                            VoteModel(name, "-1")
                        }
                        else -> {
                            ups = ups?.minus(1)
                            likes = false
                            VoteModel(name, "-1")
                        }
                    }
                }
                else -> null
            }
        }
    }

    @Parcelize
    @Entity
    @TypeConverters(value = [RedditObjectPreviewConverter::class, BaseModelConverter::class,
        RedditObjectConverter::class,
        ResolutionConverters::class])
    @Serializable
    data class RedditObjectDataWithReplies (
        var author: String? = null,
        var score: Int?= null,
        @SerialName("created_utc")
        var createdUtc: Double?= null,
        var permalink: String?= null,
        var subReddit: String?= null,
        @SerialName("subreddit_name_prefixed")
        var subredditNamePrefixed: String?= null,
        var title: String?= null,
        var url: String?= null,
        @SerialName("url_overridden_by_dest")
        var urlOverriddenByDest: String? = null,
        var preview: RedditObjectPreview?= null,
        var thumbnail: String?= null,
        @SerialName("num_comments")
        var numComments: Int?= null,
        var body: String?= null,
        var ups: Int?= null,
        var likes: Boolean? = null,
        var replies: BaseModel ?= null,
        var id: String?= null,
        @SerialName("selftext_html")
        var selfTextHtml: String? = null,
        var indexInResponse: Int = -1,
        @PrimaryKey var name: String
    ) : Parcelable, RedditObjectData()

}
