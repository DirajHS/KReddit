package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.*
import com.diraj.kreddit.data.models.RedditObjectData
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.details.FeedItemDetailsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FeedItemDetailsViewModel @Inject constructor(
    private val feedItemDetailsRepo: FeedItemDetailsRepo): ViewModel() {

    private var _feedDetailsLiveData = MutableLiveData<RedditResponse>()
    val feedDetailsLiveData: LiveData<RedditResponse>
        get() = _feedDetailsLiveData

    lateinit var feedDetailsByNameLiveData: LiveData<RedditObjectData>

    fun fetchFeedItemDetails(permalink: String) {
        viewModelScope.launch {
            _feedDetailsLiveData.postValue(RedditResponse.Loading)
            withContext(Dispatchers.IO) {
                _feedDetailsLiveData.postValue(feedItemDetailsRepo.getFeedItemDetails(permalink))
            }
        }
    }

    fun fetchFeedByName(feedName: String) {
        feedDetailsByNameLiveData = feedItemDetailsRepo
            .getFeedByName(feedName)
            .asLiveData(viewModelScope.coroutineContext)
    }
}
