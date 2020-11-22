package com.diraj.kreddit.data.repo.vote.api

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VoteAPIService {

    @POST(value = "api/vote")
    suspend fun vote(@Body voteRequestBody: RequestBody): Response<Void>
}