package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.repo.HomeFeedBoundaryCallback
import com.diraj.kreddit.presentation.home.repo.HomeFeedRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(private val homeFeedBoundaryCallback: HomeFeedBoundaryCallback,
                                            private val homeFeedRepo: HomeFeedRepo,
    kredditDB: KRedditDB): ViewModel() {

    init {
        homeFeedBoundaryCallback.coroutineScope = viewModelScope
    }

    private val config = PagedList.Config.Builder()
        .setEnablePlaceholders(true)
        .setPageSize(PAGE_SIZE)
        .setInitialLoadSizeHint(5 * PAGE_SIZE)
        .setPrefetchDistance(PREFETCH_DISTANCE)
        .build()

    private val dataSource = kredditDB.kredditPostsDAO().posts()

    val pagedFeedList: LiveData<PagedList<RedditObjectData.RedditObjectDataWithoutReplies>> =
        LivePagedListBuilder(dataSource, config)
            .setBoundaryCallback(homeFeedBoundaryCallback)
            .build()

    fun getFeedApiState() = homeFeedBoundaryCallback.feedApiStateLiveData

    fun listIsEmpty() = pagedFeedList.value?.isEmpty() ?: true

    fun retry() = homeFeedBoundaryCallback.retry()

    fun refresh() {
        viewModelScope.launch(context = Dispatchers.IO) {
            homeFeedRepo.refresh(getFeedApiState())
        }
    }

    companion object {
        const val PAGE_SIZE = 25
        const val PREFETCH_DISTANCE = 50
    }
}
