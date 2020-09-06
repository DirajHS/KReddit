package com.diraj.kreddit.presentation.home.di

import com.diraj.kreddit.di.qualifier.PerFragment
import com.diraj.kreddit.presentation.home.fragment.HomeFeedDetailsFragment
import com.diraj.kreddit.presentation.home.fragment.HomeFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeActivityModule {

    @PerFragment
    @ContributesAndroidInjector
    abstract fun providesHomeFeedFragment(): HomeFeedFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun providesHomeFeedDetailsFragment(): HomeFeedDetailsFragment
}