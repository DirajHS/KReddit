package com.diraj.kreddit.data.utils

import com.diraj.kreddit.data.models.BaseModel
import com.diraj.kreddit.data.models.CommentsData
import com.diraj.kreddit.data.models.RedditObject
import com.diraj.kreddit.data.models.RedditObjectData
import com.diraj.kreddit.data.utils.DataLayerConstants.FEED_COMMENT_KIND

class CommentsParser(private var commentsResponse: List<BaseModel>) {

    private lateinit var commentsDataSequence: Sequence<CommentsData>

    private fun processReplies(redditData: RedditObject): CommentsData {
        val redditContentData = redditData.data

        var commentList = emptyList<CommentsData>()
        when(redditContentData) {
            is RedditObjectData.RedditObjectDataWithReplies -> {
                commentList = redditContentData.replies?.data?.children
                    ?.filter { redditObject -> redditObject.kind == FEED_COMMENT_KIND }
                    ?.map {
                        val reply = processReplies(it)
                        reply
                    } ?: emptyList()
                return CommentsData(body = redditContentData.body, author = redditContentData.author,
                    score = redditContentData.score, id = redditContentData.id,
                    createdUtc = redditContentData.createdUtc?.toLong(), name = redditContentData.name,
                    children = commentList, ups = redditContentData.ups, likes = redditContentData.likes)
            }
            is RedditObjectData.RedditObjectDataWithoutReplies -> {
                return CommentsData(body = redditContentData.body, author = redditContentData.author,
                    score = redditContentData.score, id = redditContentData.id,
                    createdUtc = redditContentData.createdUtc?.toLong(),
                    name = redditContentData.name, children = commentList,
                    ups = redditContentData.ups, likes = redditContentData.likes)
            }
        }
    }

    fun parseComments(): Sequence<CommentsData> {
        commentsDataSequence = sequence {
            commentsResponse
                .asSequence()
                .flatMap { it.data.children }
                .filter { redditContent -> redditContent.kind == FEED_COMMENT_KIND }
                .forEach {
                    val processedComment = processReplies(it)
                    yield(processedComment)
                }
        }
        return commentsDataSequence
    }

}