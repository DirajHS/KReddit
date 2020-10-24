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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.LayoutHomeFeedFragmentBinding
import com.diraj.kreddit.di.GlideApp
import com.diraj.kreddit.di.Injectable
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.recyclerview.adapter.PostsAdapter
import com.diraj.kreddit.presentation.home.recyclerview.adapter.PostsLoadStateAdapter
import com.diraj.kreddit.presentation.home.viewmodel.HomeFeedViewModel
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.utils.KRedditConstants.CLICKED_DISLIKE
import com.diraj.kreddit.utils.KRedditConstants.CLICKED_LIKE
import com.diraj.kreddit.utils.androidLazy
import com.diraj.kreddit.utils.getViewModel
import com.diraj.kreddit.utils.sharedViewModel
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    private lateinit var postsAdapter: PostsAdapter

    private val tabNavHostFragment: NavHostFragment
        get() = childFragmentManager.findFragmentById(R.id.tablet_nav_container) as NavHostFragment

    @ExperimentalPagingApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        return if(!::layoutHomeFeedFragmentBinding.isInitialized) {
            layoutHomeFeedFragmentBinding =
                LayoutHomeFeedFragmentBinding.inflate(inflater, container, false)

            (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.home_fragment_title)

            val glideRequestManager = GlideApp.with(this)
            postsAdapter = PostsAdapter(glideRequestManager, this)
            initAdapter()
            handleRetryClick()
            setRefreshListener()
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

    @ExperimentalPagingApi
    private fun initAdapter() {
        layoutHomeFeedFragmentBinding.rvFeed.adapter = postsAdapter.withLoadStateHeaderAndFooter(
            header = PostsLoadStateAdapter(postsAdapter),
            footer = PostsLoadStateAdapter(postsAdapter)
        )

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            @OptIn(ExperimentalCoroutinesApi::class)
            postsAdapter.loadStateFlow.collectLatest { loadStates ->
                layoutHomeFeedFragmentBinding.swipeRefreshLayout.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            homeFeedViewModel.postsFlow.collectLatest {
                postsAdapter.submitData(it)
            }
        }

        observeAdapterLoadState()
    }

    private fun setRefreshListener() {
        layoutHomeFeedFragmentBinding.swipeRefreshLayout
            .setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.like_true_color),
                ContextCompat.getColor(requireContext(), R.color.like_false_color),
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorAccent))
        layoutHomeFeedFragmentBinding.swipeRefreshLayout.setOnRefreshListener {
            postsAdapter.refresh()
        }
    }

    private fun observeAdapterLoadState() {
        Timber.d("observeAdapterLoadState")
        postsAdapter.addLoadStateListener { loadState ->
            Timber.d("load state: ${loadState.source}")
            when(loadState.source.refresh) {
                is LoadState.Loading -> {
                    if(postsAdapter.itemCount > 0) {
                        toggleLayoutVisibility(postsVisibility = true,
                            errorVisibility = false, loadingVisibility = false)
                    } else {
                        toggleLayoutVisibility(postsVisibility = false,
                            errorVisibility = false, loadingVisibility = true)
                    }
                }
                is LoadState.NotLoading -> {
                    toggleLayoutVisibility(postsVisibility = true, errorVisibility = false,
                        loadingVisibility =  false)
                }
                is LoadState.Error -> {
                    toggleLayoutVisibility(postsVisibility = false,
                        errorVisibility = true, loadingVisibility = false)
                    layoutHomeFeedFragmentBinding.errorView.tvError.text = getErrorText((loadState.source.refresh as LoadState.Error).error)
                }
            }
            layoutHomeFeedFragmentBinding.swipeRefreshLayout.isRefreshing = (loadState.source.refresh == LoadState.Loading)
        }
    }

    private fun handleRetryClick() {
        layoutHomeFeedFragmentBinding.errorView.root.setOnClickListener {
            postsAdapter.retry()
        }
    }

    private fun getErrorText(throwableError: Throwable): String? {
        return when (throwableError) {
            is HttpException -> {
                "${throwableError.code()}: ${throwableError.message}"
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

    private fun toggleLayoutVisibility(postsVisibility: Boolean,
                                       loadingVisibility: Boolean, errorVisibility: Boolean) {
        layoutHomeFeedFragmentBinding.rvFeed.isVisible = postsVisibility
        layoutHomeFeedFragmentBinding.loadingView.root.isVisible = loadingVisibility
        layoutHomeFeedFragmentBinding.errorView.root.isVisible = errorVisibility
    }
}
