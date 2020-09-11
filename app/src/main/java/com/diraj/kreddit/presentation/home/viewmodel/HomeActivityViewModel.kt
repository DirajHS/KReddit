package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.*
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.utils.KRedditConstants.MEDIA_TYPE
import com.diraj.kreddit.utils.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val _userInfoLiveData = MutableLiveData<RedditResponse>()
    val userInfoLiveData: LiveData<RedditResponse>
        get() = _userInfoLiveData

    fun fetchProfileInfo() {
        viewModelScope.launch(context = Dispatchers.IO) {
            _userInfoLiveData.postValue(RedditResponse.Loading)
            try {
                val profileInfo =
                    redditRetrofit.create(RedditAPIService::class.java).getCurrentUserInfo()
                _userInfoLiveData.postValue(RedditResponse.Success(profileInfo))
            } catch (ex: HttpException) {
                _userInfoLiveData.postValue(RedditResponse.Error(ex))
                return@launch
            } catch (ex: UnknownHostException) {
                _userInfoLiveData.postValue(RedditResponse.Error(ex))
                return@launch
            }
        }
    }

    fun doLogout() = liveData(context = Dispatchers.IO) {
        emit(RedditResponse.Loading)
        try {

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
