package com.diraj.kreddit.presentation.home.viewmodel

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.utils.KRedditConstants.MEDIA_TYPE
import com.diraj.kreddit.utils.UserSession
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named

class HomeActivityViewModel @Inject constructor(private val redditRetrofit: Retrofit,
                                                private val redditDB: KRedditDB,
                                                @Named("Authenticator")private val authenticatorRetrofit: Retrofit)
    : ViewModel() {

    fun fetchProfileInfo() = liveData(context = Dispatchers.IO) {
        emit(RedditResponse.Loading)
        try {
            val profileInfo =
                redditRetrofit.create(RedditAPIService::class.java).getCurrentUserInfo()
            emit(RedditResponse.Success(profileInfo))
        } catch (ex: HttpException) {
            emit(RedditResponse.Error(ex))
            return@liveData
        } catch (ex: UnknownHostException) {
            emit(RedditResponse.Error(ex))
            return@liveData
        }
    }

    fun doLogout() = liveData(context = Dispatchers.IO) {
        emit(RedditResponse.Loading)
        try {
            val authString = BuildConfig.REDDIT_CLIENT_ID + ":"
            val encodedAuthString = Base64.encodeToString(authString.toByteArray(),
                Base64.NO_WRAP)

            val postInfo = "token=${UserSession.refreshToken}&token_type_hint=refresh_token"
            val postBody = postInfo.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())

            authenticatorRetrofit.create(RedditAPIService::class.java)
                .logout(postBody)
            UserSession.close()
            redditDB.kredditPostsDAO().deleteAllPosts()
            emit(RedditResponse.Success(null))
        } catch (ex: HttpException) {
            emit(RedditResponse.Error(ex))
            return@liveData
        } catch (ex: UnknownHostException) {
            emit(RedditResponse.Error(ex))
            return@liveData
        }
    }
}
