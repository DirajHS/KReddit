package com.diraj.kreddit.presentation.home.fragment

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.diraj.kreddit.databinding.LayoutHomeFeedFragmentBinding
import com.diraj.kreddit.di.Injectable
import com.diraj.kreddit.network.FeedApiState
import com.diraj.kreddit.presentation.home.epoxy.controllers.HomeFeedEpoxyController
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import timber.log.Timber
import javax.inject.Inject

class HomeFeedFragment: Fragment(), Injectable {

    @field:Inject
    lateinit var homeFeedViewModel: HomeFeedViewModel

    private lateinit var layoutHomeFeedFragmentBinding: LayoutHomeFeedFragmentBinding

    private lateinit var feedPagedEpoxyController: HomeFeedEpoxyController
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        layoutHomeFeedFragmentBinding = LayoutHomeFeedFragmentBinding.inflate(inflater, container, false)

        val handlerThread = HandlerThread("epoxy")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        feedPagedEpoxyController = HomeFeedEpoxyController({ homeFeedViewModel.retry() }, handler)
        feedPagedEpoxyController.isDebugLoggingEnabled = true

        layoutHomeFeedFragmentBinding.ervFeed.setController(feedPagedEpoxyController)
        return layoutHomeFeedFragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        observeFeedData()
        observeFeedApiState()
        handleRetryClick()
    }

    private fun observeFeedData() {
        Timber.d("observeFeedData")
        homeFeedViewModel.pagedFeedList.observe(viewLifecycleOwner, {
            Timber.d("submitting feed list")
            feedPagedEpoxyController.submitList(it)
        })
    }

    private fun observeFeedApiState() {
        Timber.d("observeFeedApiState")
        homeFeedViewModel.getFeedApiState().observe(viewLifecycleOwner, {
            if(!homeFeedViewModel.listIsEmpty()) {
                when (it) {
                    is FeedApiState.Loading -> {
                        feedPagedEpoxyController.error = null
                        feedPagedEpoxyController.isLoading = true
                    }
                    is FeedApiState.Success -> {
                        feedPagedEpoxyController.error = null
                        feedPagedEpoxyController.isLoading = false
                    }
                    is FeedApiState.Error -> {
                        feedPagedEpoxyController.isLoading = false
                        feedPagedEpoxyController.error = it.ex.message
                    }
                }
            }

            layoutHomeFeedFragmentBinding.loadingView.root.isVisible = (homeFeedViewModel.listIsEmpty() && it == FeedApiState.Loading)
            layoutHomeFeedFragmentBinding.errorView.root.isVisible = (homeFeedViewModel.listIsEmpty() && it is FeedApiState.Error)
        })
    }

    private fun handleRetryClick() {
        layoutHomeFeedFragmentBinding.errorView.root.setOnClickListener {
            homeFeedViewModel.retry()
        }
    }
}