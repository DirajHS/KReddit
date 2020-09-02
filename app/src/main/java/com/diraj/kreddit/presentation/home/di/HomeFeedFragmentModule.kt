package com.diraj.kreddit.presentation.home.di

import androidx.lifecycle.ViewModelProvider
import com.diraj.kreddit.di.qualifier.PerFragment
import com.diraj.kreddit.presentation.home.fragment.HomeFeedFragment
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import dagger.Module
import dagger.Provides

@Module
class HomeFeedFragmentModule {

    @PerFragment
    @Provides
    fun provideHomeFeedViewModel(homeFeedFragment: HomeFeedFragment,
                                 homeFeedViewModelFactory: HomeFeedViewModel.HomeFeedViewModelFactory): HomeFeedViewModel {
        return ViewModelProvider(homeFeedFragment, homeFeedViewModelFactory).get(HomeFeedViewModel::class.java)
    }
}