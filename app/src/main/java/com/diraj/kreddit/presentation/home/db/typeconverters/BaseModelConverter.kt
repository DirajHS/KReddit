package com.diraj.kreddit.presentation.home.db.typeconverters

import androidx.room.TypeConverter
import com.diraj.kreddit.network.models.BaseModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BaseModelConverter {

    @TypeConverter
    fun fromBaseModel(baseModel: BaseModel?) : String {
        if(baseModel == null)
            return ""
        return Gson().toJson(baseModel)
    }

    @TypeConverter
    fun toBaseModel(jsonBaseModel: String?) : BaseModel? {
        if(jsonBaseModel == null)
            return null
        val listType = object : TypeToken<BaseModel>() {

        }.type
        return Gson().fromJson(jsonBaseModel, listType)
    }
}
