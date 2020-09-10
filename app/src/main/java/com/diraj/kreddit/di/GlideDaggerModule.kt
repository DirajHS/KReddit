package com.diraj.kreddit.di

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GlideDaggerModule {

    @ContributesAndroidInjector
    abstract fun provideAppGlideModule(): KRedditGlideModule
}