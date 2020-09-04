package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.presentation.home.repo.HomeFeedDataSource
import com.diraj.kreddit.presentation.home.repo.HomeFeedDataSourceFactory
import retrofit2.Retrofit

class HomeFeedViewModel (val homeFeedDataSourceFactory: HomeFeedDataSourceFactory): ViewModel() {

    private val config = PagedList.Config.Builder()
        .setPageSize(PAGE_SIZE)
        .setInitialLoadSizeHint(2 * PAGE_SIZE)
        .setEnablePlaceholders(false)
        .build()

    val pagedFeedList: LiveData<PagedList<RedditObject>> by lazy {
        LivePagedListBuilder(homeFeedDataSourceFactory, config).build()
    }

    fun getFeedApiState() = homeFeedDataSourceFactory.getFeedApiStateLiveData()

    fun listIsEmpty() = pagedFeedList.value?.isEmpty() ?: true

    fun retry() = homeFeedDataSourceFactory.homeFeedDataSource.retry()

    companion object {
        const val PAGE_SIZE = 10
    }

    @Suppress("UNCHECKED_CAST")
    class HomeFeedViewModelFactory constructor(var redditRetrofIt: Retrofit): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when(modelClass) {
                HomeFeedViewModel::class.java -> {
                    val homeFeedDataSourceFactory = HomeFeedDataSourceFactory(HomeFeedDataSource(redditRetrofIt.create(RedditAPIService::class.java)))
                    val homeFeedViewModel = HomeFeedViewModel(homeFeedDataSourceFactory)
                    homeFeedViewModel.homeFeedDataSourceFactory.homeFeedDataSource.coroutineScope = homeFeedViewModel.viewModelScope
                    homeFeedViewModel
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
            } as T
        }
    }
}