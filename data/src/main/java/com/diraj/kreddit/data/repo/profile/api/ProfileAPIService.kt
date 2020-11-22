package com.diraj.kreddit.data.repo.profile.api

import com.diraj.kreddit.data.models.UserData
import retrofit2.http.GET

interface ProfileAPIService {

    @GET("api/v1/me")
    suspend fun getCurrentUserInfo(): UserData
}