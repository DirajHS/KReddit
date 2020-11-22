package com.diraj.kreddit.data.repo.details

import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.details.api.DetailsAPIService
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Inject

class FeedItemDetailsRepo @Inject constructor(private val redditRetrofit: Retrofit,
                                              private val kRedditDB: KRedditDB) {

    suspend fun getFeedItemDetails(permalink: String): RedditResponse {
        return try {
            val feedItemDetailsResponse = redditRetrofit.create(DetailsAPIService::class.java).fetchCommentsFromPermalink(permalink)
            val parsedCommentsData = com.diraj.kreddit.data.utils.CommentsParser(
                feedItemDetailsResponse
            ).parseComments()
            RedditResponse.Success(parsedCommentsData)
        } catch (ex: HttpException) {
            RedditResponse.Error(ex)
        } catch (ex: UnknownHostException) {
            RedditResponse.Error(ex)
        }
    }

    fun getFeedByName(feedName: String) = kRedditDB.kredditPostsDAO().getUniqueFeedByName(feedName)
}