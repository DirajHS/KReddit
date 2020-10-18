package com.diraj.kreddit.presentation.home.db

import androidx.paging.DataSource
import androidx.room.*
import com.diraj.kreddit.network.models.RedditObjectData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface KRedditPostsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<RedditObjectData.RedditObjectDataWithoutReplies>)

    @Query("SELECT * FROM RedditObjectDataWithoutReplies ORDER BY indexInResponse ASC")
    fun posts(): DataSource.Factory<Int, RedditObjectData.RedditObjectDataWithoutReplies>

    @Query("SELECT MAX(indexInResponse) + 1 FROM RedditObjectDataWithoutReplies")
    fun getNextIndexInReddit() : Int

    @Query("SELECT * FROM RedditObjectDataWithoutReplies WHERE name == :feedName")
    fun getFeedByName(feedName: String): Flow<RedditObjectData.RedditObjectDataWithoutReplies>

    fun getUniqueFeedByName(feedName: String) = getFeedByName(feedName).distinctUntilChanged()

    @Query("DELETE FROM RedditObjectDataWithoutReplies")
    fun deleteAllPosts()

    @Update(entity = RedditObjectData.RedditObjectDataWithoutReplies::class)
    fun updateRedditFeed(redditObjectData: RedditObjectData.RedditObjectDataWithoutReplies)
}
