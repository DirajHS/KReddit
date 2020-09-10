package com.diraj.kreddit.utils

import com.diraj.kreddit.network.models.BaseModel
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.utils.KRedditConstants.FEED_COMMENT_KIND

class CommentsParser(private var commentsResponse: List<BaseModel>) {

    private lateinit var commentsDataSequence: Sequence<CommentsData>

    private fun processReplies(redditData: RedditObject): CommentsData {
        val redditContentData = redditData.data
        var commentList = emptyList<CommentsData>()
        if (redditContentData.replies != null) {
            commentList = redditContentData.replies?.data?.children
                ?.filter { redditObject -> redditObject.kind == FEED_COMMENT_KIND }
                ?.map {
                    val reply = processReplies(it)
                    reply
                } ?: emptyList()
        }
        return CommentsData(body = redditContentData.body,
            author = redditContentData.author,
            score = redditContentData.score,
            id = redditContentData.id,
            createdUtc = redditContentData.createdUtc,
            name = redditContentData.name,
            children = commentList,
            ups = redditContentData.ups,
            likes = redditContentData.likes)
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