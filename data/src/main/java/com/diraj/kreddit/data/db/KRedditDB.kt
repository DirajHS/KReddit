package com.diraj.kreddit.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.diraj.kreddit.data.db.dao.KRedditPostsDAO
import com.diraj.kreddit.data.models.RedditObjectData
import kotlinx.serialization.json.Json

@Database(entities = [RedditObjectData.RedditObjectDataWithoutReplies::class], version = 4, exportSchema = false)
abstract class KRedditDB: RoomDatabase() {

    abstract fun kredditPostsDAO(): KRedditPostsDAO

    companion object {
        private lateinit var INSTANCE: KRedditDB

        lateinit var jsonConverter: Json

        /*
        Getting instance through companion object so that migrations can be easily added.
         */
        @Synchronized
        fun getDatabase(applicationContext: Context) : KRedditDB {
            if(!::INSTANCE.isInitialized) {
                INSTANCE = Room.databaseBuilder(
                    applicationContext, KRedditDB::class.java,
                    "kreddit.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE
        }
    }
}
