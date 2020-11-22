package com.diraj.kreddit.data.repo.vote

import com.diraj.kreddit.data.db.KRedditDB
import com.diraj.kreddit.data.models.RedditObjectData
import com.diraj.kreddit.data.models.post.VoteModel
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.repo.vote.api.VoteAPIService
import com.diraj.kreddit.data.utils.DataLayerConstants.DIR
import com.diraj.kreddit.data.utils.DataLayerConstants.ID
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class VoteRepo @Inject constructor(private val redditRetrofit: Retrofit,
                                   private val kRedditDB: KRedditDB) {
    suspend fun doVote(voteModel: VoteModel?): RedditResponse {
        Timber.d("voteModel: $voteModel")
        if(voteModel != null) {
            return try {
                val postBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(DIR, voteModel.dir)
                    .addFormDataPart(ID, voteModel.id)
                    .build()
                redditRetrofit.create(VoteAPIService::class.java).vote(voteRequestBody = postBody)
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

    fun updateDBAfterVote(updatedRedditObjectData: RedditObjectData.RedditObjectDataWithoutReplies) {
        kRedditDB.kredditPostsDAO().updateRedditFeed(updatedRedditObjectData)
    }
}