package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.RedditObjectData
import com.diraj.kreddit.data.repo.home.HomeFeedBoundaryCallback
import com.diraj.kreddit.data.repo.home.HomeFeedRepo
import com.diraj.kreddit.data.utils.DataLayerConstants.POSTS_PAGE_SIZE
import com.diraj.kreddit.data.utils.DataLayerConstants.POSTS_PREFETCH_DISTANCE
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
        .setPageSize(POSTS_PAGE_SIZE)
        .setInitialLoadSizeHint(5 * POSTS_PAGE_SIZE)
        .setPrefetchDistance(POSTS_PREFETCH_DISTANCE)
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
}
