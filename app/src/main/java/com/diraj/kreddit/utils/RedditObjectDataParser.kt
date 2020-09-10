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
        val redditContentData = RedditObjectData(name = json.asJsonObject[NAME].asString)
        val jsonObject = json.asJsonObject
        redditContentData.id = (jsonObject[ID].asString)
        if (jsonObject.has(TITLE)) {
            redditContentData.title = (jsonObject[TITLE].asString)
        }
        if (jsonObject.has(SUB_REDDIT_NAME_PREFIXED)) {
            redditContentData.subredditNamePrefixed = (jsonObject[SUB_REDDIT_NAME_PREFIXED].asString)
        }
        if (jsonObject.has(CREATED_AT)) {
            redditContentData.createdUtc = (jsonObject[CREATED_AT].asLong)
        }
        if (jsonObject.has(THUMBNAIL)) {
            redditContentData.thumbnail = (jsonObject[THUMBNAIL].asString)
        }
        if (jsonObject.has(AUTHOR)) {
            redditContentData.author = (jsonObject[AUTHOR].asString)
        }
        if (jsonObject.has(SUBREDDIT)) {
            redditContentData.subReddit = (jsonObject[SUBREDDIT].asString)
        }
        if (jsonObject.has(UPS)) {
            redditContentData.ups = (jsonObject[UPS].asInt)
        }
        if (jsonObject.has(SCORES)) {
            redditContentData.score = (jsonObject[SCORES].asInt)
        }
        if (jsonObject.has(NUM_COMMENTS)) {
            redditContentData.numComments = (jsonObject[NUM_COMMENTS].asInt)
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
        if(jsonObject.has(URL_DESTINATION)) {
            redditContentData.urlOverriddenByDest = (jsonObject[URL].asString)
        }
        if (jsonObject.has(REPLIES)) {
            val redditResponseJsonElement = jsonObject[REPLIES]
            if (redditResponseJsonElement.isJsonObject) {
                redditContentData.replies = gson.fromJson(redditResponseJsonElement, BaseModel::class.java)
            }
        }
        if(jsonObject.has(LIKES) && !jsonObject[LIKES].isJsonNull) {
            redditContentData.likes = (jsonObject[LIKES].asBoolean)
        }
        if(jsonObject.has(SELF_TEXT_HTML) && !jsonObject[SELF_TEXT_HTML].isJsonNull) {
            redditContentData.selfTextHtml = (jsonObject[SELF_TEXT_HTML].asString)
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
        const val NUM_COMMENTS = "num_comments"
        const val PREVIEW = "preview"
        const val PERMALINK = "permalink"
        const val SUB_REDDIT_NAME_PREFIXED = "subreddit_name_prefixed"
        const val CREATED_AT = "created_utc"
        const val SUBREDDIT = "subreddit"
        const val UPS = "ups"
        const val SCORES = "score"
        const val URL = "url"
        const val URL_DESTINATION = "url_overridden_by_dest"
        const val LIKES = "likes"
        const val NAME = "name"
        const val SELF_TEXT_HTML = "selftext_html"
    }

}