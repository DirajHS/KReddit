package com.diraj.kreddit.data.repo.auth

import com.diraj.kreddit.data.BuildConfig
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.AccessTokenModel
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.auth.api.AuthAPIService
import com.diraj.kreddit.data.user.UserSession
import com.diraj.kreddit.data.utils.DataLayerConstants.MEDIA_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named

class AuthenticationRepo @Inject constructor(@Named("Authenticator") var redditRetrofit: Retrofit,
                                             private val redditDB: KRedditDB) {

    fun getAccessToken(code: String) = flow<RedditResponse> {

        val grant = "grant_type=authorization_code&code=$code&redirect_uri=${BuildConfig.REDDIT_REDIRECT_URI}"

        val postBody = grant.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())
        val redditAPIService = redditRetrofit.create(AuthAPIService::class.java)
        val accessTokenModel = redditAPIService.getAccessToken(postBody)
        setUserSession(accessTokenModel)
        emit(RedditResponse.Success(accessTokenModel))
    }.catch {
        emit(RedditResponse.Error(it as Exception))
    }.flowOn(Dispatchers.IO)

    suspend fun logout() : RedditResponse {
        return try {
            val postInfo = "token=${UserSession.refreshToken}&token_type_hint=refresh_token"
            val postBody = postInfo.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())

            redditRetrofit.create(AuthAPIService::class.java)
                .logout(postBody)
            UserSession.close()
            redditDB.kredditPostsDAO().deleteAllPosts()
            RedditResponse.Success(null)
        } catch (ex: HttpException) {
            RedditResponse.Error(ex)
        } catch (ex: UnknownHostException) {
            RedditResponse.Error(ex)
        }
    }

    private fun setUserSession(accessTokenModel: AccessTokenModel) {
        val accessToken = accessTokenModel.accessToken
        val refreshToken = accessTokenModel.refreshToken
        if (refreshToken != null) {
            UserSession.open(accessToken, refreshToken)
        }
    }
}