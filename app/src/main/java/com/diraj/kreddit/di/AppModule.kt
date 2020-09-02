package com.diraj.kreddit.di

import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun providesHomeFeedViewModelFactory(redditRetrofit: Retrofit): HomeFeedViewModel.HomeFeedViewModelFactory {
        return HomeFeedViewModel.HomeFeedViewModelFactory(redditRetrofit)
    }
}