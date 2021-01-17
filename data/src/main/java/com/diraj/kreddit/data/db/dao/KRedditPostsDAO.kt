package com.diraj.kreddit.data.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.diraj.kreddit.data.models.RedditObjectDataWithoutReplies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface KRedditPostsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<RedditObjectDataWithoutReplies>)

    @Query("SELECT * FROM RedditObjectDataWithoutReplies ORDER BY indexInResponse ASC")
    fun posts(): DataSource.Factory<Int, RedditObjectDataWithoutReplies>

    @Query("SELECT MAX(indexInResponse) + 1 FROM RedditObjectDataWithoutReplies")
    fun getNextIndexInReddit() : Int

    @Query("SELECT * FROM RedditObjectDataWithoutReplies WHERE name == :feedName")
    fun getFeedByName(feedName: String): Flow<RedditObjectDataWithoutReplies>

    fun getUniqueFeedByName(feedName: String) = getFeedByName(feedName).distinctUntilChanged()

    @Query("DELETE FROM RedditObjectDataWithoutReplies")
    fun deleteAllPosts()

    @Update(entity = RedditObjectDataWithoutReplies::class)
    fun updateRedditFeed(redditObjectData: RedditObjectDataWithoutReplies)
}
