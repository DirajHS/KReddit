package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.presentation.home.repo.FeedItemDetailsRepo
import com.diraj.kreddit.utils.CommentsParser
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

class FeedItemDetailsViewModel @Inject constructor(val feedItemDetailsRepo: FeedItemDetailsRepo): ViewModel() {

    private var _feedDetailsLiveData = MutableLiveData<RedditResponse>()
    private val feedDetailsLiveData: LiveData<RedditResponse>
        get() = _feedDetailsLiveData

    fun fetchFeedItemDetails(permalink: String): LiveData<RedditResponse> {
        viewModelScope.launch {
            _feedDetailsLiveData.postValue(RedditResponse.Loading)
            try {
                val feedItemDetailsResponse = feedItemDetailsRepo.getFeedItemDetails(permalink)
                val parsedCommentsData = CommentsParser(feedItemDetailsResponse).parseComments()
                _feedDetailsLiveData.postValue(RedditResponse.Success(parsedCommentsData))
            } catch (ex: HttpException) {
                _feedDetailsLiveData.postValue(RedditResponse.Error(ex))
                return@launch
            } catch (ex: UnknownHostException) {
                _feedDetailsLiveData.postValue(RedditResponse.Error(ex))
                return@launch
            }
        }
        return feedDetailsLiveData
    }
}