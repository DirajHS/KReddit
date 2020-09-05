package com.diraj.kreddit.di

import com.diraj.kreddit.di.qualifier.PerActivity
import com.diraj.kreddit.presentation.home.HomeActivity
import com.diraj.kreddit.presentation.home.di.HomeActivityModule
import com.diraj.kreddit.presentation.login.AuthenticationActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @PerActivity
    @ContributesAndroidInjector(modules = [HomeActivityModule::class])
    abstract fun provideHomeFeedActivity(): HomeActivity

    @PerActivity
    @ContributesAndroidInjector()
    abstract fun provideAuthenticationActivity(): AuthenticationActivity
}