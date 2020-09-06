package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.network.models.RedditObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RedditObjectConverter {

    @TypeConverter
    fun fromChildren(children: List<RedditObject>) : String {
        return Gson().toJson(children)
    }

    @TypeConverter
    fun toChildren(jsonChildren: String) : List<RedditObject> {
        val listType = object : TypeToken<List<RedditObject>>() {

        }.type
        return Gson().fromJson(jsonChildren, listType)
    }
}
