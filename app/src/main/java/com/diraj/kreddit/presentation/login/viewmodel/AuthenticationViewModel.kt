package com.diraj.kreddit.presentation.login.viewmodel

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.utils.KRedditConstants.ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX
import com.diraj.kreddit.utils.KRedditConstants.ACCESS_TOKEN_KEY
import com.diraj.kreddit.utils.KRedditConstants.MEDIA_TYPE
import com.diraj.kreddit.utils.KRedditConstants.REFRESH_TOKEN_KEY
import com.diraj.kreddit.utils.KRedditConstants.USER_AGENT_VALUE
import com.diraj.kreddit.utils.UserSession
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named

class AuthenticationViewModel @Inject constructor(@Named("Authenticator") var redditRetrofit: Retrofit): ViewModel() {

    private val _accessCodeLiveData = MutableLiveData<RedditResponse>()
    private val accessCodeLiveData: LiveData<RedditResponse>
        get() = _accessCodeLiveData

    fun processAccessCode(code: String): LiveData<RedditResponse> {
        viewModelScope.launch {
            _accessCodeLiveData.postValue(RedditResponse.Loading)
            val authString = BuildConfig.REDDIT_CLIENT_ID + ":"
            val encodedAuthString = Base64.encodeToString(authString.toByteArray(),
                Base64.NO_WRAP)
            val grant = "grant_type=authorization_code&code=$code&redirect_uri=${BuildConfig.REDDIT_REDIRECT_URI}"

            val postBody = grant.toRequestBody(MEDIA_TYPE.toMediaTypeOrNull())

            val redditAPIService = redditRetrofit.create(RedditAPIService::class.java)
            try {
                val accessCodeResponse = redditAPIService.getAccessToken(USER_AGENT_VALUE,
                    "$ACCESS_TOKEN_BASIC_AUTHORIZATION_PREFIX $encodedAuthString", postBody)
                accessCodeResponse.let {
                    val accessToken = it.get(ACCESS_TOKEN_KEY).asString
                    val refreshToken = it.get(REFRESH_TOKEN_KEY).asString

                    UserSession.open(accessToken, refreshToken)
                    _accessCodeLiveData.postValue(RedditResponse.Success(null))
                }
            } catch (ex: HttpException) {
                _accessCodeLiveData.postValue(RedditResponse.Error(ex))
                return@launch
            } catch (ex: UnknownHostException) {
                _accessCodeLiveData.postValue(RedditResponse.Error(ex))
                return@launch
            }

        }

        return accessCodeLiveData
    }
}