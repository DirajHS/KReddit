package com.diraj.kreddit.data.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.Resolutions

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