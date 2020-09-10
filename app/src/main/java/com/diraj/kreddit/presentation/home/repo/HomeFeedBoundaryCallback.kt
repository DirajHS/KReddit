package com.diraj.kreddit.presentation.home.repo

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.utils.PagingRequestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.UnknownHostException
import java.util.concurrent.Executors
import javax.inject.Inject

class HomeFeedBoundaryCallback @Inject constructor(private val kRedditDB: KRedditDB,
                               kredditRetrofit: Retrofit): PagedList.BoundaryCallback<RedditObjectData>() {

    private val executor = Executors.newSingleThreadExecutor()
    private val helper = PagingRequestHelper(executor)

    val feedApiStateLiveData = MutableLiveData<RedditResponse>()
    private val redditAPIService: RedditAPIService = kredditRetrofit.create(RedditAPIService::class.java)

    lateinit var coroutineScope: CoroutineScope

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helperCallback ->
            feedApiStateLiveData.postValue(RedditResponse.Loading)
            coroutineScope.launch(context = Dispatchers.IO) {
                try {
                    val feedData = redditAPIService.getHomeFeed(after = null, limit = 25)
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
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: RedditObjectData) {
        super.onItemAtEndLoaded(itemAtEnd)
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
            feedApiStateLiveData.postValue(RedditResponse.Loading)
            coroutineScope.launch(context = Dispatchers.IO) {
                try {
                    val feedData = redditAPIService.getHomeFeed(after = itemAtEnd.name, limit = 25)
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
        }
    }

    private fun insertFeed(baseModel: BaseModel) {
        val redditFeedList = mutableListOf<RedditObjectData>()
        val start = kRedditDB.kredditPostsDAO().getNextIndexInReddit()
        baseModel.data.children.mapIndexed { index, redditObject ->
            val redditObjectData = redditObject.data
            redditObjectData.indexInResponse = start + index
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
