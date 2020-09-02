package com.diraj.kreddit.network

import com.diraj.kreddit.network.models.BaseModel
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditAPIService {

    @GET(".json")
    suspend fun getHomeFeed(@Query("after") after: String?, @Query("limit") limit: Int): BaseModel
}