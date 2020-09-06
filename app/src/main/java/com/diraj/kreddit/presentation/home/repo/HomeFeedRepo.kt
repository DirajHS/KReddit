package com.diraj.kreddit.presentation.home.repo

import com.diraj.kreddit.db.KRedditDB
import com.diraj.kreddit.network.RedditAPIService
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.network.models.post.VoteModel
import com.diraj.kreddit.utils.KRedditConstants
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Inject

class HomeFeedRepo @Inject constructor(private val kRedditDB: KRedditDB, private  val  kreddiRetrofit: Retrofit) {

    suspend fun doVote(clickedBtnType: String, redditObject: RedditObjectData) {
        val voteModel = when(clickedBtnType) {
            KRedditConstants.CLICKED_LIKE -> {
                if(redditObject.likes == true) {
                    redditObject.ups = redditObject.ups?.minus(1)
                    redditObject.likes = null
                    VoteModel(redditObject.name, "0")
                } else {
                    redditObject.ups = redditObject.ups?.plus(1)
                    redditObject.likes = true
                    VoteModel(redditObject.name, "1")
                }
            }
            KRedditConstants.CLICKED_DISLIKE -> {
                if(redditObject.likes == false) {
                    redditObject.likes = null
                    VoteModel(redditObject.name, "0")
                } else {
                    redditObject.likes = false
                    VoteModel(redditObject.name, "-1")
                }
            }
            else -> null
        }
        if(voteModel != null) {
            try {
                val postBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(KRedditConstants.DIR, voteModel.dir)
                    .addFormDataPart(KRedditConstants.ID, voteModel.id)
                    .build()
                kreddiRetrofit.create(RedditAPIService::class.java).vote(voteRequestBody = postBody)
                kRedditDB.kredditPostsDAO().updateRedditFeed(redditObject)
            } catch (ex: HttpException) {
                Timber.e("error voting: ${ex.message()}")
            } catch (ex: UnknownHostException) {
                Timber.e("No network")
            }
        }
    }
}
