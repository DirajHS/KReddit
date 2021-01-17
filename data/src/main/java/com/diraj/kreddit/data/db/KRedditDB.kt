package com.diraj.kreddit.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.diraj.kreddit.data.db.dao.KRedditPostsDAO
import com.diraj.kreddit.data.models.RedditObjectDataWithoutReplies
import kotlinx.serialization.json.Json

@Database(entities = [RedditObjectDataWithoutReplies::class], version = 4, exportSchema = false)
abstract class KRedditDB: RoomDatabase() {

    abstract fun kredditPostsDAO(): KRedditPostsDAO

    companion object {
        private lateinit var INSTANCE: KRedditDB
        private const val DB_NAME = "kreddit.db"

        lateinit var jsonConverter: Json

        /*
        Getting instance through companion object so that migrations can be easily added.
         */
        @Synchronized
        fun getDatabase(applicationContext: Context) : KRedditDB {
            if(!::INSTANCE.isInitialized) {
                INSTANCE = Room.databaseBuilder(
                    applicationContext, KRedditDB::class.java,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE
        }
    }
}
