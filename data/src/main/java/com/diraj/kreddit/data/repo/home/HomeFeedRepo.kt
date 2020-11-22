package com.diraj.kreddit.data.repo.home

import androidx.lifecycle.MutableLiveData
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.BaseModel
import com.diraj.kreddit.data.models.RedditObjectData
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.home.api.HomeAPIService
import com.diraj.kreddit.data.utils.DataLayerConstants.POSTS_PAGE_SIZE
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Inject

class HomeFeedRepo @Inject constructor(private val kRedditDB: KRedditDB,
                                       private val kredditRetrofit: Retrofit) {

    suspend fun refresh(feedRefreshAPIState: MutableLiveData<RedditResponse>) {
        feedRefreshAPIState.postValue(RedditResponse.Loading)
        val kredditAPIService = kredditRetrofit.create(HomeAPIService::class.java)
        try {
            val feedata = kredditAPIService.getHomeFeed(after = null, limit = POSTS_PAGE_SIZE * 2)
            kRedditDB.kredditPostsDAO().deleteAllPosts()
            insertFeed(feedata)
        }  catch (ex: HttpException) {
            feedRefreshAPIState.postValue(RedditResponse.Error(ex))
        } catch (ex: UnknownHostException) {
            feedRefreshAPIState.postValue(RedditResponse.Error(ex))
        }
    }

    private fun insertFeed(baseModel: BaseModel) {
        val redditFeedList = mutableListOf<RedditObjectData.RedditObjectDataWithoutReplies>()
        val start = kRedditDB.kredditPostsDAO().getNextIndexInReddit()
        baseModel.data.children.mapIndexed { index, redditObject ->
            val redditObjectData = (redditObject.data
                    as RedditObjectData.RedditObjectDataWithoutReplies).copy(indexInResponse = start + index)
            redditObjectData
        }.forEach {
            redditFeedList.add(it)
        }
        kRedditDB.kredditPostsDAO().insert(redditFeedList)
    }
}