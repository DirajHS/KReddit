package com.diraj.kreddit.presentation.home.repo

import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.BaseModel
import retrofit2.Retrofit
import javax.inject.Inject

class FeedItemDetailsRepo @Inject constructor(private val redditRetrofit: Retrofit,
                                              private val kRedditDB: KRedditDB) {

    suspend fun getFeedItemDetails(permalink: String): List<BaseModel> {
        return redditRetrofit.create(RedditAPIService::class.java).fetchCommentsFromPermalink(permalink)
    }

    fun getFeedByName(feedName: String) = kRedditDB.kredditPostsDAO().getUniqueFeedByName(feedName)
}