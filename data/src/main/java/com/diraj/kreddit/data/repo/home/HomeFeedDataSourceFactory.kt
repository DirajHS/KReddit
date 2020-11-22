package com.diraj.kreddit.data.repo.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.diraj.kreddit.data.models.RedditObject
import com.diraj.kreddit.data.repo.home.api.HomeAPIService
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit


class HomeFeedDataSourceFactory (var redditRetrofit: Retrofit)
    : DataSource.Factory<String, RedditObject>() {

    lateinit var homeFeedDataSource: HomeFeedDataSource

    lateinit var viewModeScope: CoroutineScope

    var homeFeeDataSourceLiveData = MutableLiveData<HomeFeedDataSource>()

    override fun create(): DataSource<String, RedditObject> {
        homeFeedDataSource = HomeFeedDataSource(redditRetrofit.create(HomeAPIService::class.java))
        homeFeedDataSource.coroutineScope = viewModeScope
        homeFeeDataSourceLiveData.postValue(homeFeedDataSource)
        return homeFeedDataSource
    }

    fun getFeedApiStateLiveData(): LiveData<HomeFeedDataSource> = homeFeeDataSourceLiveData
}
