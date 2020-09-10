package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.network.models.Resolutions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ResolutionConverters {

    @TypeConverter
    fun fromObjectPreview(preview: Resolutions?) : String {
        if(preview == null)
            return ""
        return Gson().toJson(preview)
    }

    @TypeConverter
    fun toObjectPreview(jsonObject: String?) : Resolutions? {
        if(jsonObject == null)
            return null
        val listType = object : TypeToken<Resolutions>() {

        }.type
        return Gson().fromJson(jsonObject, listType)
    }
}