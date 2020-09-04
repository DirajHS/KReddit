package com.diraj.kreddit.presentation.home.repo

import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.BaseModel
import retrofit2.Retrofit
import javax.inject.Inject

class FeedItemDetailsRepo @Inject constructor(val redditRetrofit: Retrofit) {

    suspend fun getFeedItemDetails(permalink: String): List<BaseModel> {
        return redditRetrofit.create(RedditAPIService::class.java).fetchCommentsPermalink(permalink)
    }
}