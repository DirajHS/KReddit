package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.models.RedditObject
import kotlinx.serialization.builtins.ListSerializer

class RedditObjectConverter {

    @TypeConverter
    fun fromChildren(children: List<RedditObject>?) : String {
        if(children == null)
            return ""
        return KRedditDB.jsonConverter.encodeToString(ListSerializer(RedditObject.serializer()),
            children)
    }

    @TypeConverter
    fun toChildren(jsonChildren: String?) : List<RedditObject>? {
        if(jsonChildren == null || jsonChildren.isEmpty())
            return null
        return KRedditDB.jsonConverter.decodeFromString(ListSerializer(RedditObject.serializer()),
            jsonChildren)
    }
}
