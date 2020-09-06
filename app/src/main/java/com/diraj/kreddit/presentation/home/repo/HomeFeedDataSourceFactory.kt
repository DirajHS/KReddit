package com.diraj.kreddit.presentation.home.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.RedditObject
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit


class HomeFeedDataSourceFactory (var redditRetrofit: Retrofit)
    : DataSource.Factory<String, RedditObject>() {

    lateinit var homeFeedDataSource: HomeFeedDataSource

    lateinit var viewModeScope: CoroutineScope

    var homeFeeDataSourceLiveData = MutableLiveData<HomeFeedDataSource>()

    override fun create(): DataSource<String, RedditObject> {
        homeFeedDataSource = HomeFeedDataSource(redditRetrofit.create(RedditAPIService::class.java))
        homeFeedDataSource.coroutineScope = viewModeScope
        homeFeeDataSourceLiveData.postValue(homeFeedDataSource)
        return homeFeedDataSource
    }

    fun getFeedApiStateLiveData(): LiveData<HomeFeedDataSource> = homeFeeDataSourceLiveData
}
