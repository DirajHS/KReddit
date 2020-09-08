package com.diraj.kreddit.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.network.models.post.VoteModel
import com.diraj.kreddit.utils.KRedditConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class SharedViewModel @Inject constructor(private val redditRetrofit: Retrofit,
                                          private val kRedditDB: KRedditDB): ViewModel() {

    private lateinit var voteJob: Job

    fun vote(clickedBtnType: String, redditObject: RedditObjectData) {
        if(::voteJob.isInitialized) {
            voteJob.cancel()
        }
        voteJob = viewModelScope.launch(context = Dispatchers.IO) {
            doVote(clickedBtnType, redditObject)
        }
    }

    private suspend fun doVote(clickedBtnType: String, redditObjectData: RedditObjectData) {
        val voteModel = getVoteModelToPost(clickedBtnType, redditObjectData)
        if(voteModel != null) {
            try {
                val postBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(KRedditConstants.DIR, voteModel.dir)
                    .addFormDataPart(KRedditConstants.ID, voteModel.id)
                    .build()
                redditRetrofit.create(RedditAPIService::class.java).vote(voteRequestBody = postBody)
                kRedditDB.kredditPostsDAO().updateRedditFeed(redditObjectData)
            } catch (ex: HttpException) {
                Timber.e("error voting: ${ex.message()}")
            } catch (ex: UnknownHostException) {
                Timber.e("No network")
            }
        }
    }

    private fun getVoteModelToPost(clickedBtnType: String, redditObjectData: RedditObjectData): VoteModel? {
        return when(clickedBtnType) {
            KRedditConstants.CLICKED_LIKE -> {
                when (redditObjectData.likes) {
                    true -> {
                        redditObjectData.ups = redditObjectData.ups?.minus(1)
                        redditObjectData.likes = null
                        VoteModel(redditObjectData.name, "0")
                    }
                    false -> {
                        redditObjectData.ups = redditObjectData.ups?.plus(2)
                        redditObjectData.likes = true
                        VoteModel(redditObjectData.name, "1")
                    }
                    else -> {
                        redditObjectData.ups = redditObjectData.ups?.plus(1)
                        redditObjectData.likes = true
                        VoteModel(redditObjectData.name, "1")
                    }
                }
            }
            KRedditConstants.CLICKED_DISLIKE -> {
                when (redditObjectData.likes) {
                    false -> {
                        redditObjectData.ups = redditObjectData.ups?.plus(1)
                        redditObjectData.likes = null
                        VoteModel(redditObjectData.name, "0")
                    }
                    true -> {
                        redditObjectData.ups = redditObjectData.ups?.minus(2)
                        redditObjectData.likes = false
                        VoteModel(redditObjectData.name, "-1")
                    }
                    else -> {
                        redditObjectData.ups = redditObjectData.ups?.minus(1)
                        redditObjectData.likes = false
                        VoteModel(redditObjectData.name, "-1")
                    }
                }
            }
            else -> null
        }
    }
}