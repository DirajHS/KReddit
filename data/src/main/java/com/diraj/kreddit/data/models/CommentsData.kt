package com.diraj.kreddit.data.models

import android.os.Parcelable
import com.diraj.kreddit.data.models.post.VoteModel
import com.diraj.kreddit.data.utils.DataLayerConstants.CLICKED_DISLIKE
import com.diraj.kreddit.data.utils.DataLayerConstants.CLICKED_LIKE
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
            CLICKED_LIKE -> {
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
            CLICKED_DISLIKE -> {
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