package com.diraj.kreddit.data.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.RedditObject
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
