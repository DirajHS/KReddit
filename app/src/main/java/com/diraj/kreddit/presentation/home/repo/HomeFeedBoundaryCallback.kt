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
import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.Executors
import javax.inject.Inject

class HomeFeedBoundaryCallback @Inject constructor(private val kRedditDB: KRedditDB,
                               kredditRetrofit: Retrofit): PagedList.BoundaryCallback<RedditObjectData>() {

    private lateinit var retryExecutable: () -> Unit

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
                    kRedditDB.kredditPostsDAO().insert(getRedditFeed(feedData))
                    helperCallback.recordSuccess()
                    feedApiStateLiveData.postValue(RedditResponse.Success(null))
                } catch (ex: HttpException) {
                    setRetry { onZeroItemsLoaded() }
                    feedApiStateLiveData.postValue(RedditResponse.Error(ex))
                } catch (ex: UnknownHostException) {
                    setRetry { onZeroItemsLoaded() }
                    feedApiStateLiveData.postValue(RedditResponse.Error(ex)) //TODO: Use interceptor to broadcast no network
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
                    kRedditDB.kredditPostsDAO().insert(getRedditFeed(feedData))
                    helperCallback.recordSuccess()
                    feedApiStateLiveData.postValue(RedditResponse.Success(null))
                } catch (ex: HttpException) {
                    setRetry { onItemAtEndLoaded(itemAtEnd) }
                    feedApiStateLiveData.postValue(RedditResponse.Error(ex))
                } catch (ex: UnknownHostException) {
                    setRetry { onItemAtEndLoaded(itemAtEnd) }
                    feedApiStateLiveData.postValue(RedditResponse.Error(ex)) //TODO: Use interceptor to broadcast no network
                }
            }
        }
    }

    private fun getRedditFeed(baseModel: BaseModel): List<RedditObjectData> {
        val redditFeedList = mutableListOf<RedditObjectData>()
        baseModel.data.children.forEach {
            redditFeedList.add(it.data)
        }
        return redditFeedList
    }

    private fun setRetry(retryRef: () -> Unit) {
        retryExecutable = retryRef
    }

    fun retry() {
        if(::retryExecutable.isInitialized) {
            Timber.d("calling retry")
            retryExecutable.invoke()
        }
    }
}
