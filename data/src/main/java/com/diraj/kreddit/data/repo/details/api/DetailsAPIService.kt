package com.diraj.kreddit.data.repo.details.api

import com.diraj.kreddit.data.models.BaseModel
import retrofit2.http.GET
import retrofit2.http.Path

interface DetailsAPIService {

    @GET("{permalink}.json")
    suspend fun fetchCommentsFromPermalink(@Path("permalink", encoded = true) permalink: String) : List<BaseModel>
}