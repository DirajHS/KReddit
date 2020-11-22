package com.diraj.kreddit.data.repo.profile

import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.profile.api.ProfileAPIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit
import javax.inject.Inject

class ProfileRepo @Inject constructor(private val redditRetrofit: Retrofit) {

    fun fetchProfileInfo() = flow<RedditResponse> {
        val profileInfo = redditRetrofit.create(ProfileAPIService::class.java).getCurrentUserInfo()
        emit(RedditResponse.Success(profileInfo))
    }.catch {
        emit(RedditResponse.Error(it as Exception))
    }.flowOn(Dispatchers.IO)
}