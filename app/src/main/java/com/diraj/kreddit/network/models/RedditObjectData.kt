package com.diraj.kreddit.network.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RedditObjectData (
    var author: String? = null,
    var score: Int?= null,
    var created_utc: Long?= null,
    var permalink: String?= null,
    var subreddit: String?= null,
    var subreddit_name_prefixed: String?= null,
    var title: String?= null,
    var url: String?= null,
    var preview: RedditObjectPreview?= null,
    var thumbnail: String?= null,
    var num_comments: Int?= null,
    var body: String?= null,
    var ups: Int?= null,
    var replies: BaseModel?= null,
    var id: String?= null
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