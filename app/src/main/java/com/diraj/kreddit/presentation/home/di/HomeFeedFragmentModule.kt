package com.diraj.kreddit.presentation.home.di

import android.os.Handler
import android.os.HandlerThread
import com.diraj.kreddit.BuildConfig
import com.diraj.kreddit.di.GlideApp
import com.diraj.kreddit.di.GlideRequests
import com.diraj.kreddit.presentation.home.epoxy.controllers.HomeFeedEpoxyController
import com.diraj.kreddit.presentation.home.fragment.HomeFeedFragment
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import dagger.Module
import dagger.Provides

@Module
class HomeFeedFragmentModule {

    @Provides
    fun providesGlideRequestManager(homeFeedFragment: HomeFeedFragment): GlideRequests =
        GlideApp.with(homeFeedFragment)

    @Provides
    fun providesEpoxyDiffHandler(): Handler {
        val handlerThread = HandlerThread("epoxyDiffHandlerThread")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }

    @Provides
    fun providesHomeFeedEpoxyController(homeFeedFragment: HomeFeedFragment,
                                        homeFeedViewModel: HomeFeedViewModel,
                                        diffHandler: Handler,
                                        glideRequests: GlideRequests): HomeFeedEpoxyController {
        val homeFeedEpoxyController = HomeFeedEpoxyController({ homeFeedViewModel.retry() },
            diffHandler, homeFeedFragment, glideRequests)
        homeFeedEpoxyController.isDebugLoggingEnabled = BuildConfig.DEBUG
        return homeFeedEpoxyController
    }
}
