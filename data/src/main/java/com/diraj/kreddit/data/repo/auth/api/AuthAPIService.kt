package com.diraj.kreddit.data.repo.auth.api

import com.diraj.kreddit.data.models.AccessTokenModel
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthAPIService {

    @POST(value = "api/v1/access_token")
    suspend fun getAccessToken(@Body postBody: RequestBody): AccessTokenModel

    @POST(value = "api/v1/revoke_token")
    suspend fun logout(@Body postBody: RequestBody) : Response<Void>
}