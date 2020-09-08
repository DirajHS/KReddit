package com.diraj.kreddit.network.models

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.diraj.kreddit.presentation.home.db.typeconverters.BaseModelConverter
import com.diraj.kreddit.presentation.home.db.typeconverters.RedditObjectConverter
import com.diraj.kreddit.presentation.home.db.typeconverters.RedditObjectPreviewConverter
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
@TypeConverters(value = [RedditObjectPreviewConverter::class, BaseModelConverter::class, RedditObjectConverter::class])
data class RedditObjectData (
    var author: String? = null,
    var score: Int?= null,
    var created_utc: Long?= null,
    var permalink: String?= null,
    var subreddit: String?= null,
    var subreddit_name_prefixed: String?= null,
    var title: String?= null,
    var url: String?= null,
    var url_overridden_by_dest: String? = null,
    var preview: RedditObjectPreview?= null,
    var thumbnail: String?= null,
    var num_comments: Int?= null,
    var body: String?= null,
    var ups: Int?= null,
    var likes: Boolean? = null,
    var replies: BaseModel ?= null,
    var id: String?= null,
    var indexInResponse: Int = -1,
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
}
