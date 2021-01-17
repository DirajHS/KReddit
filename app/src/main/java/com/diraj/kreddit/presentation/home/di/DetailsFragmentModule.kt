package com.diraj.kreddit.presentation.home.di

import com.diraj.kreddit.di.GlideApp
import com.diraj.kreddit.di.GlideRequests
import com.diraj.kreddit.presentation.home.fragment.HomeFeedDetailsFragment
import dagger.Module
import dagger.Provides

@Module
class DetailsFragmentModule {

    @Provides
    fun providesGlideRequests(homeFeedDetailsFragment: HomeFeedDetailsFragment): GlideRequests =
        GlideApp.with(homeFeedDetailsFragment)
}
