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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor( private val homeFeedRepo: HomeFeedRepo,
    private val homeFeedBoundaryCallback: HomeFeedBoundaryCallback,
    kredditDB: KRedditDB): ViewModel() {

    init {
        homeFeedBoundaryCallback.coroutineScope = viewModelScope
    }

    private val config = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setPageSize(PAGE_SIZE)
        .setInitialLoadSizeHint(5 * PAGE_SIZE)
        .setPrefetchDistance(PREFETCH_DISTANCE)
        .build()

    private val dataSource = kredditDB.kredditPostsDAO().posts()

    val pagedFeedList: LiveData<PagedList<RedditObjectData>> =
        LivePagedListBuilder(dataSource, config)
            .setBoundaryCallback(homeFeedBoundaryCallback)
            .build()


    private lateinit var voteJob: Job

    fun getFeedApiState() = homeFeedBoundaryCallback.feedApiStateLiveData

    fun listIsEmpty() = pagedFeedList.value?.isEmpty() ?: true

    fun retry() = homeFeedBoundaryCallback.retry()

    fun vote(clickedBtnType: String, redditObject: RedditObjectData) {
        if(::voteJob.isInitialized) {
            voteJob.cancel()
        }
        voteJob = viewModelScope.launch(context = Dispatchers.IO) {
            homeFeedRepo.doVote(clickedBtnType, redditObject)
        }
    }

    companion object {
        const val PAGE_SIZE = 25
        const val PREFETCH_DISTANCE = 50
    }
}
