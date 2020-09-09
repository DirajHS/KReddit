package com.diraj.kreddit.presentation.home.repo

import androidx.lifecycle.MutableLiveData
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Inject

class HomeFeedRepo @Inject constructor(private val kRedditDB: KRedditDB,
                                       private val kredditRetrofit: Retrofit) {

    suspend fun refresh(feedRefreshAPIState: MutableLiveData<RedditResponse>) {
        feedRefreshAPIState.postValue(RedditResponse.Loading)
        val kredditAPIService = kredditRetrofit.create(RedditAPIService::class.java)
        try {
            val feedata = kredditAPIService.getHomeFeed(after = null, limit = HomeFeedViewModel.PAGE_SIZE * 2)
            kRedditDB.kredditPostsDAO().deleteAllPosts()
            insertFeed(feedata)
        }  catch (ex: HttpException) {
            feedRefreshAPIState.postValue(RedditResponse.Error(ex))
        } catch (ex: UnknownHostException) {
            feedRefreshAPIState.postValue(RedditResponse.Error(ex))
        }
    }

    private fun insertFeed(baseModel: BaseModel) {
        val redditFeedList = mutableListOf<RedditObjectData>()
        val start = kRedditDB.kredditPostsDAO().getNextIndexInSubreddit()
        baseModel.data.children.mapIndexed { index, redditObject ->
            val redditObjectData = redditObject.data
            redditObjectData.indexInResponse = start + index
            redditObjectData
        }.forEach {
            redditFeedList.add(it)
        }
        kRedditDB.kredditPostsDAO().insert(redditFeedList)
    }
}