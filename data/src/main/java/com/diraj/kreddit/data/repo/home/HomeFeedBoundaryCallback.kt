package com.diraj.kreddit.data.repo.home

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.BaseModel
import com.diraj.kreddit.data.models.RedditObjectData
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.home.api.HomeAPIService
import com.diraj.kreddit.data.utils.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import java.util.concurrent.Executors
import javax.inject.Inject

class HomeFeedBoundaryCallback @Inject constructor(private val kRedditDB: KRedditDB,
                               kredditRetrofit: Retrofit):
    PagedList.BoundaryCallback<RedditObjectData.RedditObjectDataWithoutReplies>() {

    private val executor = Executors.newSingleThreadExecutor()
    private val helper = com.diraj.kreddit.data.utils.PagingRequestHelper(executor)

    val feedApiStateLiveData = MutableLiveData<RedditResponse>()
    private val redditAPIService: HomeAPIService = kredditRetrofit.create(HomeAPIService::class.java)

    lateinit var coroutineScope: CoroutineScope

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            feedApiStateLiveData.postValue(RedditResponse.Loading)
            coroutineScope.launch(context = Dispatchers.IO) {
                fetchHomeFeed(null, helperCallback)
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: RedditObjectData.RedditObjectDataWithoutReplies) {
        super.onItemAtEndLoaded(itemAtEnd)
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            feedApiStateLiveData.postValue(RedditResponse.Loading)
            coroutineScope.launch(context = Dispatchers.IO) {
                fetchHomeFeed(itemAtEnd.name, helperCallback)
            }
        }
    }

    private suspend fun fetchHomeFeed(after: String?,
                                      helperCallback: PagingRequestHelper.Request.Callback?) {
        try {
            val feedData = redditAPIService.getHomeFeed(after = after, limit = 25)
            insertFeed(feedData)
            helperCallback?.recordSuccess()
            feedApiStateLiveData.postValue(RedditResponse.Success(null))
        } catch (ex: HttpException) {
            helperCallback?.recordFailure(ex)
            feedApiStateLiveData.postValue(RedditResponse.Error(ex))
        } catch (ex: UnknownHostException) {
            helperCallback?.recordFailure(ex)
            feedApiStateLiveData.postValue(RedditResponse.Error(ex))
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

    fun retry() {
        helper.retryAllFailed()
    }
}
