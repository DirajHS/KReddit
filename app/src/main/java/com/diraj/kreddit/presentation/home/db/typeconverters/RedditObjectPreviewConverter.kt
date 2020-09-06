package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.network.models.RedditObjectPreview
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RedditObjectPreviewConverter {

    @TypeConverter
    fun fromObjectPreview(preview: RedditObjectPreview?) : String {
        if(preview == null)
            return ""
        return Gson().toJson(preview)
    }

    @TypeConverter
    fun toObjectPreview(jsonObject: String?) : RedditObjectPreview? {
        if(jsonObject == null)
            return null
        val listType = object : TypeToken<RedditObjectPreview>() {

        }.type
        return Gson().fromJson(jsonObject, listType)
    }
}
