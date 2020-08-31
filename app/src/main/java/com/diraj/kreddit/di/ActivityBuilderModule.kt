package com.diraj.kreddit.di

import com.diraj.kreddit.MainActivity
import com.diraj.kreddit.di.qualifier.PerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @PerActivity
    @ContributesAndroidInjector
    abstract fun provideMainActivity(): MainActivity
}