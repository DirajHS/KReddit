package com.diraj.kreddit.di

import com.diraj.kreddit.KReddit
import com.diraj.kreddit.db.KRedditDB
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideKredditDB(kreddit: KReddit, jsonInstance: Json): KRedditDB {
        KRedditDB.jsonConverter = jsonInstance
        return KRedditDB.getDatabase(kreddit)
    }
}
