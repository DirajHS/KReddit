package com.diraj.kreddit.data.network.authenticators

import com.diraj.kreddit.data.repo.auth.api.AuthAPIService
import com.diraj.kreddit.data.user.UserSession
import com.diraj.kreddit.data.user.UserSession.accessToken
import com.diraj.kreddit.data.user.UserSession.refreshToken
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION
import com.diraj.kreddit.data.utils.DataLayerConstants.AUTHORIZATION_HEADER_PREFIX_BEARER
import com.diraj.kreddit.data.utils.DataLayerConstants.MEDIA_TYPE
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okio.IOException
import retrofit2.HttpException
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
        val grant = "grant_type=refresh_token&refresh_token=$refreshToken"

        val postBody = grant.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())

        val redditAPIService = authenticatorRetrofit.create(AuthAPIService::class.java)
        try {
            return runBlocking {
                val accessCodeResponse = redditAPIService.getAccessToken(postBody)
                val accessToken = accessCodeResponse.accessToken
                UserSession.open(accessToken, refreshToken!!)
                return@runBlocking accessToken
            }
        } catch (ex: HttpException) {
          return null
        } catch (ex: IOException) {
            return null
        }
    }

    private fun authHeaderValue(accessTok:String?): String = "$AUTHORIZATION_HEADER_PREFIX_BEARER $accessTok"

}