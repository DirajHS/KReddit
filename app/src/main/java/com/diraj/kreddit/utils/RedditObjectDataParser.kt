package com.diraj.kreddit.utils

import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.network.models.RedditObjectPreview
import com.google.gson.*
import java.lang.reflect.Type

class RedditObjectDataParser : JsonDeserializer<RedditObjectData?> {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(RedditObjectData::class.java, this)
        .create()

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RedditObjectData {
        val redditContentData = RedditObjectData()
        val jsonObject = json.asJsonObject
        redditContentData.id = (jsonObject["id"].asString)
        if (jsonObject.has(TITLE)) {
            redditContentData.title = (jsonObject[TITLE].asString)
        }
        if (jsonObject.has(SUBREDDITNAMEPREFIXED)) {
            redditContentData.subreddit_name_prefixed = (jsonObject[SUBREDDITNAMEPREFIXED].asString)
        }
        if (jsonObject.has(CREATED_AT)) {
            redditContentData.created_utc = (jsonObject[CREATED_AT].asLong)
        }
        if (jsonObject.has(THUMBNAIL)) {
            redditContentData.thumbnail = (jsonObject[THUMBNAIL].asString)
        }
        if (jsonObject.has(AUTHOR)) {
            redditContentData.author = (jsonObject[AUTHOR].asString)
        }
        if (jsonObject.has(SUBREDDIT)) {
            redditContentData.subreddit = (jsonObject[SUBREDDIT].asString)
        }
        if (jsonObject.has(UPS)) {
            redditContentData.ups = (jsonObject[UPS].asInt)
        }
        if (jsonObject.has(SCORES)) {
            redditContentData.score = (jsonObject[SCORES].asInt)
        }
        if (jsonObject.has(NUMCOMMENTS)) {
            redditContentData.num_comments = (jsonObject[NUMCOMMENTS].asInt)
        }
        if (jsonObject.has(BODY_HTML)) {
            redditContentData.body = (jsonObject[BODY_HTML].asString)
        }
        if (jsonObject.has(PERMALINK)) {
            redditContentData.permalink = (jsonObject[PERMALINK].asString)
        }
        if (jsonObject.has(PREVIEW)) {
            val previewJsonElement = jsonObject[PREVIEW]
            redditContentData.preview = (gson.fromJson(previewJsonElement, RedditObjectPreview::class.java))
        }
        if (jsonObject.has(URL)) {
            redditContentData.url = (jsonObject[URL].asString)
        }
        if (jsonObject.has(REPLIES)) {
            val redditResponseJsonElement = jsonObject[REPLIES]
            if (redditResponseJsonElement.isJsonObject) {
                redditContentData.replies = gson.fromJson(redditResponseJsonElement, BaseModel::class.java)
            }
        }
        return redditContentData
    }

    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val BODY_HTML = "body"
        const val REPLIES = "replies"
        const val OVER18 = "over_18"
        const val AUTHOR = "author"
        const val THUMBNAIL = "thumbnail"
        const val NUMCOMMENTS = "num_comments"
        const val PREVIEW = "preview"
        const val PERMALINK = "permalink"
        const val SUBREDDITNAMEPREFIXED = "subreddit_name_prefixed"
        const val CREATED_AT = "created_utc"
        const val SUBREDDIT = "subreddit"
        const val UPS = "ups"
        const val SCORES = "score"
        const val URL = "url"
    }

}