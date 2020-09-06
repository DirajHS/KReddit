package com.diraj.kreddit.presentation.home.db

import androidx.paging.DataSource
import androidx.room.*
import com.diraj.kreddit.network.models.RedditObjectData

@Dao
interface KRedditPostsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(posts: List<RedditObjectData>)

    @Query("SELECT * FROM RedditObjectData")
    fun posts(): DataSource.Factory<Int, RedditObjectData>

    @Query("DELETE FROM RedditObjectData")
    fun deleteAllPosts()

    @Update(entity = RedditObjectData::class)
    fun updateRedditFeed(redditObjectData: RedditObjectData)
}
