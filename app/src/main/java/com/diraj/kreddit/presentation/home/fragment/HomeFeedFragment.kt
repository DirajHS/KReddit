package com.diraj.kreddit.presentation.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import com.diraj.kreddit.R
import com.diraj.kreddit.data.models.RedditObjectDataWithoutReplies
import com.diraj.kreddit.data.network.RedditResponse
import com.diraj.kreddit.data.utils.DataLayerConstants.CLICKED_DISLIKE
import com.diraj.kreddit.data.utils.DataLayerConstants.CLICKED_LIKE
import com.diraj.kreddit.databinding.LayoutHomeFeedFragmentBinding
import com.diraj.kreddit.di.Injectable
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.presentation.home.epoxy.controllers.HomeFeedEpoxyController
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.utils.androidLazy
import com.diraj.kreddit.utils.sharedViewModel
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class HomeFeedFragment: Fragment(), Injectable, IFeedClickListener {

    @Inject
    lateinit var homeFeedViewModel: HomeFeedViewModel

    @Inject
    lateinit var sharedViewModelFactory: ViewModelFactory<SharedViewModel>

    @Inject
    lateinit var feedPagedEpoxyController: HomeFeedEpoxyController

    private val sharedViewModel by androidLazy {
        sharedViewModel<SharedViewModel>(sharedViewModelFactory)
    }

    private lateinit var layoutHomeFeedFragmentBinding: LayoutHomeFeedFragmentBinding

    private val tabNavHostFragment: NavHostFragment
        get() = childFragmentManager.findFragmentById(R.id.tablet_nav_container) as NavHostFragment

    private val feedAPIStateObserver = Observer<RedditResponse> { redditResponse ->
        processFeedApiState(redditResponse)
    }

    private val feedDataObserver = Observer<PagedList<RedditObjectDataWithoutReplies>> {
        Timber.d("submitting feed list")
        feedPagedEpoxyController.submitList(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView")
        return if(!::layoutHomeFeedFragmentBinding.isInitialized) {
            layoutHomeFeedFragmentBinding = LayoutHomeFeedFragmentBinding.inflate(inflater, container, false)

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

    override fun onFeedItemClicked(view: View, redditObject: RedditObjectDataWithoutReplies) {
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
                doNavigateToDestination(view, redditObject)
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
        homeFeedViewModel.pagedFeedList.observe(viewLifecycleOwner, feedDataObserver)
    }

    private fun observeFeedApiState() {
        Timber.d("observeFeedApiState")
        homeFeedViewModel.getFeedApiState().observe(viewLifecycleOwner, feedAPIStateObserver)
    }

    private fun processFeedApiState(redditResponse: RedditResponse) {
        if(!homeFeedViewModel.listIsEmpty()) {
            feedPagedEpoxyController.error = if(redditResponse is RedditResponse.Error)
                getErrorText(redditResponse) else null
            feedPagedEpoxyController.isLoading = redditResponse is RedditResponse.Loading
        }

        layoutHomeFeedFragmentBinding.apply {
            loadingView.root.isVisible = (homeFeedViewModel.listIsEmpty() &&
                    redditResponse == RedditResponse.Loading)
            errorView.root.isVisible = (homeFeedViewModel.listIsEmpty() &&
                    redditResponse is RedditResponse.Error)
            swipeRefreshLayout.isRefreshing = (redditResponse == RedditResponse.Loading)
        }

        if(layoutHomeFeedFragmentBinding.errorView.root.isVisible) {
            feedPagedEpoxyController.error = getErrorText(redditResponse as RedditResponse.Error)
        }
    }

    private fun handleRetryClick() {
        layoutHomeFeedFragmentBinding.errorView.root.setOnClickListener {
            homeFeedViewModel.retry()
        }
    }

    private fun getErrorText(redditResponse: RedditResponse.Error): String? {
        return when (redditResponse.ex) {
            is HttpException -> {
                "${(redditResponse.ex as HttpException).code()}: ${redditResponse.ex.message}"
            }
            else -> {
                getString(R.string.generic_error_string)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun doNavigateToDestination(view: View, redditObject: RedditObjectDataWithoutReplies) {
        val isTablet = requireContext().resources.getBoolean(R.bool.isTablet)

        when {
            isTablet -> {
                tabNavHostFragment.navController.navigate(HomeFeedDetailsFragmentDirections
                    .actionToHomeFeedDetailsFragment(redditObject))
            }
            else -> {
                val extras = FragmentNavigatorExtras((view
                        to redditObject.thumbnail)
                        as Pair<View, String>)
                findNavController().navigate(HomeFeedFragmentDirections
                    .actionHomeFeedFragmentToHomeFeedDetailsFragment(redditObject), extras)
            }
        }
    }
}
