package com.diraj.kreddit.data.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.BaseModel
import kotlinx.serialization.decodeFromString
import timber.log.Timber

class BaseModelConverter {

    @TypeConverter
    fun fromBaseModel(baseModel: BaseModel?) : String {
        if(baseModel == null)
            return ""
        return KRedditDB.jsonConverter.encodeToString(BaseModel.serializer(), baseModel)
    }

    @TypeConverter
    fun toBaseModel(jsonBaseModel: String?) : BaseModel? {
        if(jsonBaseModel == null || jsonBaseModel.isEmpty())
            return null
        Timber.d("json: $jsonBaseModel")
        return KRedditDB.jsonConverter.decodeFromString<BaseModel>(jsonBaseModel)
    }
}
