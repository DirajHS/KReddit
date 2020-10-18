package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.models.RedditObjectPreview
import kotlinx.serialization.decodeFromString

class RedditObjectPreviewConverter {

    @TypeConverter
    fun fromObjectPreview(preview: RedditObjectPreview?) : String {
        if(preview == null)
            return ""
        return KRedditDB.jsonConverter.encodeToString(RedditObjectPreview.serializer(), preview)
    }

    @TypeConverter
    fun toObjectPreview(jsonObject: String?) : RedditObjectPreview? {
        if(jsonObject == null || jsonObject.isEmpty())
            return null
        return KRedditDB.jsonConverter.decodeFromString<RedditObjectPreview>(jsonObject)
    }
}
