package com.diraj.kreddit.presentation.home.fragment

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.LayoutHomeFeedFragmentBinding
import com.diraj.kreddit.di.GlideApp
import com.diraj.kreddit.di.Injectable
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.epoxy.controllers.HomeFeedEpoxyController
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.utils.KRedditConstants.CLICKED_DISLIKE
import com.diraj.kreddit.utils.KRedditConstants.CLICKED_LIKE
import com.diraj.kreddit.utils.androidLazy
import com.diraj.kreddit.utils.getViewModel
import com.diraj.kreddit.utils.sharedViewModel
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class HomeFeedFragment: Fragment(), Injectable, IFeedClickListener {

    @field:Inject
    lateinit var viewModelFactory: ViewModelFactory<HomeFeedViewModel>

    @field:Inject
    lateinit var sharedViewModelFactory: ViewModelFactory<SharedViewModel>

    private val homeFeedViewModel by androidLazy {
        getViewModel<HomeFeedViewModel>(viewModelFactory)
    }

    private val sharedViewModel by androidLazy {
        sharedViewModel<SharedViewModel>(sharedViewModelFactory)
    }

    private lateinit var layoutHomeFeedFragmentBinding: LayoutHomeFeedFragmentBinding

    private lateinit var feedPagedEpoxyController: HomeFeedEpoxyController
    private lateinit var handler: Handler

    private val tabNavHostFragment: NavHostFragment
        get() = childFragmentManager.findFragmentById(R.id.tablet_nav_container) as NavHostFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        return if(!::layoutHomeFeedFragmentBinding.isInitialized) {
            layoutHomeFeedFragmentBinding = LayoutHomeFeedFragmentBinding.inflate(inflater, container, false)

            val glideRequestManager = GlideApp.with(this)
            val handlerThread = HandlerThread("epoxy")
            handlerThread.start()
            handler = Handler(handlerThread.looper)
            feedPagedEpoxyController = HomeFeedEpoxyController({ homeFeedViewModel.retry() }, handler, this, glideRequestManager)
            feedPagedEpoxyController.isDebugLoggingEnabled = true

            layoutHomeFeedFragmentBinding.ervFeed.setController(feedPagedEpoxyController)
            layoutHomeFeedFragmentBinding.root
        } else {
            layoutHomeFeedFragmentBinding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.home_fragment_title)
        observeFeedData()
        observeFeedApiState()
        handleRetryClick()
        setRefreshListener()
    }

    override fun onFeedItemClicked(view: View, redditObject: RedditObjectData.RedditObjectDataWithoutReplies) {
        when(view.id) {
            R.id.iv_thumb_up -> {
                sharedViewModel.vote(CLICKED_LIKE, redditObject)
            }
            R.id.iv_thumb_down -> {
                sharedViewModel.vote(CLICKED_DISLIKE, redditObject)
            }
            else -> {
                exitTransition = Hold().apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }

                reenterTransition = MaterialElevationScale(true).apply {
                    duration = resources.getInteger(R.integer.motion_duration_small).toLong()
                }
                doNavigateToDestination(view, RedditObject("", redditObject))
            }
        }
    }

    private fun setRefreshListener() {
        layoutHomeFeedFragmentBinding.swipeRefreshLayout
            .setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.like_true_color),
                ContextCompat.getColor(requireContext(), R.color.like_false_color),
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorAccent))
        layoutHomeFeedFragmentBinding.swipeRefreshLayout.setOnRefreshListener {
            homeFeedViewModel.refresh()
        }
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
                    is RedditResponse.Loading -> {
                        feedPagedEpoxyController.error = null
                        feedPagedEpoxyController.isLoading = true
                    }
                    is RedditResponse.Success<*> -> {
                        feedPagedEpoxyController.error = null
                        feedPagedEpoxyController.isLoading = false
                    }
                    is RedditResponse.Error -> {
                        feedPagedEpoxyController.isLoading = false
                        feedPagedEpoxyController.error = getErrorText(redditResponse = it)
                    }
                }
            }

            layoutHomeFeedFragmentBinding.loadingView.root.isVisible = (homeFeedViewModel.listIsEmpty() &&
                    it == RedditResponse.Loading)
            layoutHomeFeedFragmentBinding.errorView.root.isVisible = (homeFeedViewModel.listIsEmpty() &&
                    it is RedditResponse.Error)
            if(layoutHomeFeedFragmentBinding.errorView.root.isVisible) {
                feedPagedEpoxyController.error = getErrorText(it as RedditResponse.Error)
            }
            layoutHomeFeedFragmentBinding.swipeRefreshLayout.isRefreshing = (it == RedditResponse.Loading)
        })
    }

    private fun handleRetryClick() {
        layoutHomeFeedFragmentBinding.errorView.root.setOnClickListener {
            homeFeedViewModel.retry()
        }
    }

    private fun getErrorText(redditResponse: RedditResponse.Error): String? {
        return when (redditResponse.ex) {
            is HttpException -> {
                "${redditResponse.ex.code()}: ${redditResponse.ex.message}"
            }
            else -> {
                getString(R.string.generic_error_string)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun doNavigateToDestination(view: View, redditObject: RedditObject) {
        val isTablet = requireContext().resources.getBoolean(R.bool.isTablet)

        when {
            isTablet -> {
                tabNavHostFragment.navController.navigate(HomeFeedDetailsFragmentDirections
                    .actionToHomeFeedDetailsFragment(redditObject))
            }
            else -> {
                val extras = FragmentNavigatorExtras((view
                        to (redditObject.data as RedditObjectData.RedditObjectDataWithoutReplies).thumbnail)
                        as Pair<View, String>)
                findNavController().navigate(HomeFeedFragmentDirections
                    .actionHomeFeedFragmentToHomeFeedDetailsFragment(redditObject), extras)
            }
        }
    }
}
