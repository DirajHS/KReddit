package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.models.Resolutions

class ResolutionConverters {

    @TypeConverter
    fun fromObjectPreview(preview: Resolutions?) : String {
        if(preview == null)
            return ""
        return KRedditDB.jsonConverter.encodeToString(Resolutions.serializer(), preview)
    }

    @TypeConverter
    fun toObjectPreview(jsonObject: String?) : Resolutions? {
        if(jsonObject == null)
            return null
        return KRedditDB.jsonConverter.decodeFromString(Resolutions.serializer(), jsonObject)
    }
}