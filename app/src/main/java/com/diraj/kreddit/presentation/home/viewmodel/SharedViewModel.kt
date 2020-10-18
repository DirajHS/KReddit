package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.network.models.post.VoteModel
import com.diraj.kreddit.utils.KRedditConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class SharedViewModel @Inject constructor(private val redditRetrofit: Retrofit,
                                          private val kRedditDB: KRedditDB): ViewModel() {

    val commentsLikeDisLikeMapping = mutableMapOf<String, MutableLiveData<CommentsData>>()

    fun voteComment(clickedBtnType: String, commentsData: CommentsData): LiveData<CommentsData> {
        val likesUpdateLiveData = MutableLiveData<CommentsData>()
        commentsLikeDisLikeMapping[commentsData.name] = likesUpdateLiveData
        viewModelScope.launch(context = Dispatchers.IO) {
            when(doVote(commentsData.getVoteModelToPost(clickedBtnType))) {
                is RedditResponse.Success<*> -> {
                    commentsLikeDisLikeMapping[commentsData.name]?.postValue(commentsData)
                }
            }
        }
        return likesUpdateLiveData
    }

    fun vote(clickedBtnType: String, redditObjectDataWithoutReplies: RedditObjectData.RedditObjectDataWithoutReplies) {
        viewModelScope.launch(context = Dispatchers.IO) {
            val voteModel = redditObjectDataWithoutReplies.getVoteModelToPost(clickedBtnType)
            when(doVote(voteModel)) {
                is RedditResponse.Success<*> -> {
                    val updatedRedditObjectData = redditObjectDataWithoutReplies.copy(ups = voteModel?.ups,
                        likes = voteModel?.likes)
                    kRedditDB.kredditPostsDAO().updateRedditFeed(updatedRedditObjectData)
                }
            }
        }
    }

    private suspend fun doVote(voteModel: VoteModel?): RedditResponse {
        Timber.d("voteModel: $voteModel")
        if(voteModel != null) {
            return try {
                val postBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(KRedditConstants.DIR, voteModel.dir)
                    .addFormDataPart(KRedditConstants.ID, voteModel.id)
                    .build()
                redditRetrofit.create(RedditAPIService::class.java).vote(voteRequestBody = postBody)
                RedditResponse.Success(null)
            } catch (ex: HttpException) {
                Timber.e("error voting: ${ex.message()}")
                RedditResponse.Error(ex)
            } catch (ex: UnknownHostException) {
                Timber.e("No network")
                RedditResponse.Error(ex)
            }
        }
        return RedditResponse.Error(IllegalArgumentException("VoteModel is null"))
    }
}
