package com.diraj.kreddit.network

import android.util.Base64
import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.utils.KRedditConstants.ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX
import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION
import com.diraj.kreddit.utils.KRedditConstants.AUTHORIZATION_HEADER_PREFIX_BEARER
import com.diraj.kreddit.utils.KRedditConstants.MEDIA_TYPE
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_VALUE
import com.diraj.kreddit.utils.UserSession
import com.diraj.kreddit.utils.UserSession.accessToken
import com.diraj.kreddit.utils.UserSession.refreshToken
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okio.IOException
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class AccessTokenAuthenticator @Inject constructor(
    @Named("Authenticator")private val authenticatorRetrofit: Retrofit) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        val failedAuthRequest = response.request
        Timber.d("authenticate for ${failedAuthRequest.url}")
        val currentToken = failedAuthRequest.header(AUTHORIZATION)

        Timber.d("authHeaderValue(accessToken) == currentToken = ${authHeaderValue(accessToken) == currentToken}")

        return when {
            authHeaderValue(accessToken) != currentToken -> {
                Timber.d("proceeding new request with existing correct token")
                return response.request.newBuilder().header(AUTHORIZATION, authHeaderValue(
                    accessToken)
                ).build()
            }
            isAuthTokenError(response) -> {
                val tokenRefreshResult = doTokenRefresh()
                tokenRefreshResult?.let { newAccessToken ->
                    val newHeader = authHeaderValue(newAccessToken)
                    Timber.d("proceeding new request with new token")
                    return response.request.newBuilder().header(AUTHORIZATION, newHeader).build()
                }
            }
            else -> {
                null
            }
        }
    }

    private fun isAuthTokenError(response: Response) = response.code == 401


    private fun doTokenRefresh(): String? {
        val authString = BuildConfig.REDDIT_CLIENT_ID + ":"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(),
            Base64.NO_WRAP)
        val grant = "grant_type=refresh_token&refresh_token=$refreshToken"

        val postBody = grant.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())

        val redditAPIService = authenticatorRetrofit.create(RedditAPIService::class.java)
        try {
            return runBlocking {
                val accessCodeResponse = redditAPIService.getAccessToken(USER_AGENT_VALUE,
                    "$ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX $encodedAuthString", postBody)
                val accessToken = accessCodeResponse.access_token
                UserSession.open(accessToken, refreshToken!!)
                return@runBlocking accessToken
            }
        } catch (ex: IOException) {
            return null
        }
    }

    private fun authHeaderValue(accessTok:String?): String = "$AUTHORIZATION_HEADER_PREFIX_BEARER $accessTok"

}