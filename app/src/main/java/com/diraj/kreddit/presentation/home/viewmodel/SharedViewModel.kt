package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diraj.kreddit.data.models.CommentsData
import com.diraj.kreddit.data.models.RedditObjectDataWithoutReplies
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.vote.VoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SharedViewModel @Inject constructor(private val voteRepo: VoteRepo) : ViewModel() {

    val commentsLikeDisLikeMapping = mutableMapOf<String, MutableLiveData<CommentsData>>()

    fun voteComment(clickedBtnType: String, commentsData: CommentsData): LiveData<CommentsData> {
        val likesUpdateLiveData = MutableLiveData<CommentsData>()
        commentsLikeDisLikeMapping[commentsData.name] = likesUpdateLiveData
        viewModelScope.launch(context = Dispatchers.IO) {
            when(voteRepo.doVote(commentsData.getVoteModelToPost(clickedBtnType))) {
                is RedditResponse.Success<*> -> {
                    commentsLikeDisLikeMapping[commentsData.name]?.postValue(commentsData)
                }
            }
        }
        return likesUpdateLiveData
    }

    fun vote(clickedBtnType: String, redditObjectDataWithoutReplies: RedditObjectDataWithoutReplies) {
        viewModelScope.launch(context = Dispatchers.IO) {
            val voteModel = redditObjectDataWithoutReplies.getVoteModelToPost(clickedBtnType)
            when(voteRepo.doVote(voteModel)) {
                is RedditResponse.Success<*> -> {
                    val updatedRedditObjectData = redditObjectDataWithoutReplies.copy(ups = voteModel?.ups,
                        likes = voteModel?.likes)
                    voteRepo.updateDBAfterVote(updatedRedditObjectData)
                }
            }
        }
    }
}
