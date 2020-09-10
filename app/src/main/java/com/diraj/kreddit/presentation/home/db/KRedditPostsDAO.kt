package com.diraj.kreddit.presentation.home.db

import androidx.paging.DataSource
import androidx.room.*
import com.diraj.kreddit.network.models.RedditObjectData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface KRedditPostsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<RedditObjectData>)

    @Query("SELECT * FROM RedditObjectData ORDER BY indexInResponse ASC")
    fun posts(): DataSource.Factory<Int, RedditObjectData>

    @Query("SELECT MAX(indexInResponse) + 1 FROM RedditObjectData")
    fun getNextIndexInReddit() : Int

    @Query("SELECT * FROM RedditObjectData WHERE name == :feedName")
    fun getFeedByName(feedName: String): Flow<RedditObjectData>

    fun getUniqueFeedByName(feedName: String) = getFeedByName(feedName).distinctUntilChanged()

    @Query("DELETE FROM RedditObjectData")
    fun deleteAllPosts()

    @Update(entity = RedditObjectData::class)
    fun updateRedditFeed(redditObjectData: RedditObjectData)
}
