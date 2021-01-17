package com.diraj.kreddit.data.repo.home.api

import com.diraj.kreddit.data.models.BaseModel
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeAPIService {

    @GET(".json")
    suspend fun getHomeFeed(@Query("after") after: String?, @Query("limit") limit: Int): BaseModel

}