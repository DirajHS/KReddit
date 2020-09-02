package com.diraj.kreddit.presentation.home.repo

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.diraj.kreddit.network.FeedApiState
import com.diraj.kreddit.network.models.RedditObject


class HomeFeedDataSourceFactory (var homeFeedDataSource: HomeFeedDataSource)
    : DataSource.Factory<String, RedditObject>() {

    override fun create(): DataSource<String, RedditObject> {
        return homeFeedDataSource
    }

    fun getFeedApiStateLiveData(): LiveData<FeedApiState> = homeFeedDataSource.feedApiStateLiveData
}