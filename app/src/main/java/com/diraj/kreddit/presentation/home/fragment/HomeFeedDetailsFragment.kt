package com.diraj.kreddit.presentation.home.fragment

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diraj.kreddit.databinding.LayoutFeedItemDetailsFragmentBinding
import com.diraj.kreddit.di.Injectable
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.presentation.home.groupie.ExpandableCommentGroup
import com.diraj.kreddit.presentation.home.viewmodel.FeedItemDetailsViewModel
import com.diraj.kreddit.utils.KRedditConstants.FEED_DETAILS_MOTION_PROGRESS_KEY
import com.diraj.kreddit.utils.KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY
import com.diraj.kreddit.utils.KRedditConstants.REDDIT_OBJECT_PARCELABLE_KEY
import com.diraj.kreddit.utils.androidLazy
import com.diraj.kreddit.utils.getPrettyCount
import com.diraj.kreddit.utils.getViewModel
import com.google.android.material.transition.MaterialContainerTransform
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class HomeFeedDetailsFragment: Fragment(), Injectable {

    @field:Inject
    lateinit var viewModelFactory: ViewModelFactory<FeedItemDetailsViewModel>

    private val feedItemDetailsViewModel by androidLazy {
        getViewModel<FeedItemDetailsViewModel>(viewModelFactory)
    }

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var groupLayoutManager: GridLayoutManager

    private lateinit var layoutFeedItemDetailsFragmentBinding: LayoutFeedItemDetailsFragmentBinding

    private var redditObject: RedditObject ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = SHARED_ELEMENT_TRANSITION_DURATION
            isElevationShadowEnabled = true
            setAllContainerColors(ContextCompat.getColor(requireContext(),android.R.color.white))
        }
        super.onCreate(savedInstanceState)
        redditObject = arguments?.getParcelable(REDDIT_OBJECT_PARCELABLE_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutFeedItemDetailsFragmentBinding = LayoutFeedItemDetailsFragmentBinding.inflate(inflater,
            container, false)

        if(savedInstanceState != null) {
            layoutFeedItemDetailsFragmentBinding.mlFeedDetails.progress = savedInstanceState
                .getFloat(FEED_DETAILS_MOTION_PROGRESS_KEY, 0f)
        }
        initAdapter()
        return layoutFeedItemDetailsFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        layoutFeedItemDetailsFragmentBinding.mlFeedDetails.transitionName = redditObject?.data?.thumbnail
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        (requireActivity() as AppCompatActivity).supportActionBar?.title = redditObject?.data?.subreddit_name_prefixed
        if(redditObject == null) {
            layoutFeedItemDetailsFragmentBinding.emptyFeed.root.isVisible = true
        } else {
            layoutFeedItemDetailsFragmentBinding.emptyFeed.root.isVisible = false
            renderFeedDetails()
            fetchAndObserveFeedComments()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("onSaveInstanceState")
        outState.putFloat(FEED_DETAILS_MOTION_PROGRESS_KEY,
            layoutFeedItemDetailsFragmentBinding.mlFeedDetails.progress)
    }

    private fun initAdapter() {
        groupLayoutManager = GridLayoutManager(requireContext(), groupAdapter.spanCount).apply {
            spanSizeLookup = groupAdapter.spanSizeLookup
        }

        layoutFeedItemDetailsFragmentBinding.rvFeedItemComments.apply {
            layoutManager = groupLayoutManager
            adapter = groupAdapter
        }
    }

    private fun fetchAndObserveFeedComments() {
        redditObject?.data?.permalink?.let {
            feedItemDetailsViewModel.fetchFeedItemDetails(it).observe(viewLifecycleOwner, { redditResponse ->
                when(redditResponse) {
                    is RedditResponse.Loading -> {
                        Timber.d("loading comments")
                        //show loader only if no comments are added
                        if(groupAdapter.groupCount <= 0)
                            layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible = true
                    }
                    is RedditResponse.Success<*> -> {
                        if(layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible)
                            layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible = false
                        (redditResponse.successData as Sequence<*>).forEach { commentsData ->
                            groupAdapter.add(ExpandableCommentGroup(commentsData as CommentsData))
                        }
                    }
                    is RedditResponse.Error -> {
                        Timber.d("observed error fetching comments")
                        //TODO: show retry view
                    }
                }
            })
        }
    }

    private fun renderFeedDetails() {
        renderFeedDetailsImage()
        layoutFeedItemDetailsFragmentBinding.ivDetailTitle.text = redditObject?.data?.title
        layoutFeedItemDetailsFragmentBinding.inclFeedInfo.tvSubreddit.text = redditObject?.data?.subreddit_name_prefixed
        layoutFeedItemDetailsFragmentBinding.inclFeedInfo.tvDomain.text = redditObject?.data?.getDomain()
        layoutFeedItemDetailsFragmentBinding.inclFeedInfo.tvAuthor.text = redditObject?.data?.author
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvUps.text = redditObject?.data?.ups?.getPrettyCount()
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvComments.text = redditObject?.data?.num_comments?.getPrettyCount()
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvTime.text = PrettyTime(Locale.getDefault())
            .format(redditObject?.data?.created_utc?.times(1000L)?.let { Date(it) })

        handleDomainClick()
    }

    private fun renderFeedDetailsImage() {
        redditObject?.data?.preview?.images?.first()?.source?.let { source ->
            layoutFeedItemDetailsFragmentBinding.ivDetailImage.visibility = View.VISIBLE
            ConstraintSet().apply {
                clone(layoutFeedItemDetailsFragmentBinding.clFeedDetails)
                layoutFeedItemDetailsFragmentBinding.ivDetailImage.id.let {
                    setDimensionRatio(it, "${source.width}:${source.height}")
                }
                applyTo(layoutFeedItemDetailsFragmentBinding.clFeedDetails)
            }
            Glide.with(requireContext())
                .load(source.url?.replace(FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.1f)
                .into(layoutFeedItemDetailsFragmentBinding.ivDetailImage)
        } ?: run {
            layoutFeedItemDetailsFragmentBinding.ivDetailImage.visibility = View.GONE
        }
    }

    private fun handleDomainClick() {
        redditObject?.data?.url_overridden_by_dest?.let { destinationURL ->
            layoutFeedItemDetailsFragmentBinding.inclFeedInfo.tvDomain.setOnClickListener {
                val intent = Intent(ACTION_VIEW)
                intent.data = Uri.parse(destinationURL)
                startActivity(intent)
            }
        }

    }

    companion object {
        private const val SHARED_ELEMENT_TRANSITION_DURATION = 300L
    }
}