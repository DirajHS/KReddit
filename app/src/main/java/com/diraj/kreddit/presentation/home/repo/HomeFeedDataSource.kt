package com.diraj.kreddit.presentation.home.repo

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.RedditObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.UnknownHostException

class HomeFeedDataSource (private val redditAPIService: RedditAPIService): PageKeyedDataSource<String, RedditObject>() {

    private lateinit var retryExecutable: () -> Unit

    lateinit var coroutineScope: CoroutineScope

    val feedApiStateLiveData = MutableLiveData<RedditResponse>()

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, RedditObject>
    ) {
        feedApiStateLiveData.postValue(RedditResponse.Loading)
        coroutineScope.launch(context = Dispatchers.IO) {
            try {
                val feedData = redditAPIService.getHomeFeed(null, params.requestedLoadSize)
                feedApiStateLiveData.postValue(RedditResponse.Success(null))
                callback.onResult(feedData.data.children, null, feedData.data.after)
            } catch (ex: HttpException) {
                setRetry { loadInitial(params, callback) }
                feedApiStateLiveData.postValue(RedditResponse.Error(ex))
            } catch (ex: UnknownHostException) {
                setRetry { loadInitial(params, callback) }
                feedApiStateLiveData.postValue(RedditResponse.Error(ex)) //TODO: Use interceptor to broadcast no network
            }
        }

    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, RedditObject>) {}

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, RedditObject>) {
        feedApiStateLiveData.postValue(RedditResponse.Loading)
        coroutineScope.launch(context = Dispatchers.IO) {
            try {
                val feedData = redditAPIService.getHomeFeed(params.key, params.requestedLoadSize)
                feedApiStateLiveData.postValue(RedditResponse.Success(null))
                callback.onResult(feedData.data.children, feedData.data.after)
            } catch (ex: HttpException) {
                setRetry { loadAfter(params, callback) }
                feedApiStateLiveData.postValue(RedditResponse.Error(ex))
            }  catch (ex: UnknownHostException) {
                setRetry { loadAfter(params, callback) }
                feedApiStateLiveData.postValue(RedditResponse.Error(ex))
            }
        }
    }

    private fun setRetry(retryRef: () -> Unit) {
        retryExecutable = retryRef
    }

    fun retry() {
        if(::retryExecutable.isInitialized) {
            retryExecutable.invoke()
        }
    }
}