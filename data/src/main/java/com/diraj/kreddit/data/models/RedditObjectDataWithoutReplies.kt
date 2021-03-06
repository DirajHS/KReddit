package com.diraj.kreddit.data.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.diraj.kreddit.data.db.typeconverters.BaseModelConverter
import com.diraj.kreddit.data.db.typeconverters.RedditObjectConverter
import com.diraj.kreddit.data.db.typeconverters.RedditObjectPreviewConverter
import com.diraj.kreddit.data.db.typeconverters.ResolutionConverters
import com.diraj.kreddit.data.models.post.VoteModel
import com.diraj.kreddit.data.utils.DataLayerConstants
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName

@Parcelize
@Entity
@TypeConverters(value = [RedditObjectPreviewConverter::class, BaseModelConverter::class,
    RedditObjectConverter::class,
    ResolutionConverters::class])
@kotlinx.serialization.Serializable
data class RedditObjectDataWithoutReplies (
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
    val replies: String ?= null,
    val id: String?= null,
    @SerialName("selftext_html")
    val selfTextHtml: String? = null,
    val indexInResponse: Int = -1,
    @PrimaryKey var name: String
) : Parcelable {

    fun getDomain(): String? {
        return try {
            val host = Uri.parse(url).host
            if (host?.startsWith("www.") == true) host.substring(4) else host
        } catch (ex: Exception) {
            null
        }
    }

    fun getVoteModelToPost(clickedBtnType: String): VoteModel? {
        val newUps: Int?
        val newLikes: Boolean?
        return when(clickedBtnType) {
            DataLayerConstants.CLICKED_LIKE -> {
                when (likes) {
                    true -> {
                        newUps = ups?.minus(1)
                        newLikes = null
                        VoteModel(name, "0", newUps, newLikes)
                    }
                    false -> {
                        newUps = ups?.plus(2)
                        newLikes = true
                        VoteModel(name, "1", newUps, newLikes)
                    }
                    else -> {
                        newUps = ups?.plus(1)
                        newLikes = true
                        VoteModel(name, "1", newUps, newLikes)
                    }
                }
            }
            DataLayerConstants.CLICKED_DISLIKE -> {
                when (likes) {
                    false -> {
                        newUps = ups?.plus(1)
                        newLikes = null
                        VoteModel(name, "0", newUps, newLikes)
                    }
                    true -> {
                        newUps = ups?.minus(2)
                        newLikes = false
                        VoteModel(name, "-1", newUps, newLikes)
                    }
                    else -> {
                        newUps = ups?.minus(1)
                        newLikes = false
                        VoteModel(name, "-1", newUps, newLikes)
                    }
                }
            }
            else -> null
        }
    }
}
