package com.diraj.kreddit.network.models

import android.os.Parcelable
import com.diraj.kreddit.network.models.post.VoteModel
import com.diraj.kreddit.utils.KRedditConstants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentsData(
    val id : String?,
    val author: String?,
    val score: Int?,
    val createdUtc: Long?,
    val body: String?,
    val name: String,
    var ups: Int?,
    var likes: Boolean?,
    var children : List<CommentsData>?
): Parcelable {
    fun getVoteModelToPost(clickedBtnType: String): VoteModel? {
        return when(clickedBtnType) {
            KRedditConstants.CLICKED_LIKE -> {
                when (likes) {
                    true -> {
                        ups = ups?.minus(1)
                        likes = null
                        VoteModel(name, "0")
                    }
                    false -> {
                        ups = ups?.plus(2)
                        likes = true
                        VoteModel(name, "1")
                    }
                    else -> {
                        ups = ups?.plus(1)
                        likes = true
                        VoteModel(name, "1")
                    }
                }
            }
            KRedditConstants.CLICKED_DISLIKE -> {
                when (likes) {
                    false -> {
                        ups = ups?.plus(1)
                        likes = null
                        VoteModel(name, "0")
                    }
                    true -> {
                        ups = ups?.minus(2)
                        likes = false
                        VoteModel(name, "-1")
                    }
                    else -> {
                        ups = ups?.minus(1)
                        likes = false
                        VoteModel(name, "-1")
                    }
                }
            }
            else -> null
        }
    }
}