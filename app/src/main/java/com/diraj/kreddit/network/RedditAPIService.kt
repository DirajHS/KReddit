package com.diraj.kreddit.network

import com.diraj.kreddit.network.models.AccessTokenModel
import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.UserData
import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_KEY
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RedditAPIService {

    @GET(".json")
    suspend fun getHomeFeed(@Query("after") after: String?, @Query("limit") limit: Int): BaseModel

    @GET("{permalink}.json")
    suspend fun fetchCommentsFromPermalink(@Path("permalink", encoded = true) permalink: String) : List<BaseModel>

    @POST(value = "api/v1/access_token")
    suspend fun getAccessToken(@Header(USER_AGENT_KEY) userAgent: String,
                               @Header(AUTHORIZATION) authValue: String,
                               @Body postBody: RequestBody): AccessTokenModel

    @GET("api/v1/me")
    suspend fun getCurrentUserInfo(): UserData

    @POST(value = "api/v1/revoke_token")
    suspend fun logout(@Header(AUTHORIZATION) authValue: String,
                       @Body postBody: RequestBody) : Response<Any?>

    @POST(value = "api/vote")
    suspend fun vote(@Body voteRequestBody: RequestBody): Response<Any?>
}
