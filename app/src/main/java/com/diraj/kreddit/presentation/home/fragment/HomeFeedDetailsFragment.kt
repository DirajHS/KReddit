package com.diraj.kreddit.presentation.home.fragment

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.LayoutFeedItemDetailsFragmentBinding
import com.diraj.kreddit.di.Injectable
import com.diraj.kreddit.di.ViewModelFactory
import com.diraj.kreddit.network.RedditResponse
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.groupie.ExpandableCommentGroup
import com.diraj.kreddit.presentation.home.viewmodel.FeedItemDetailsViewModel
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.utils.*
import com.diraj.kreddit.utils.KRedditConstants.CLICKED_DISLIKE
import com.diraj.kreddit.utils.KRedditConstants.CLICKED_LIKE
import com.diraj.kreddit.utils.KRedditConstants.FEED_DETAILS_MOTION_PROGRESS_KEY
import com.diraj.kreddit.utils.KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY
import com.diraj.kreddit.utils.KRedditConstants.REDDIT_OBJECT_PARCELABLE_KEY
import com.google.android.material.textview.MaterialTextView
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

    @field:Inject
    lateinit var sharedViewModelFactory: ViewModelFactory<SharedViewModel>

    private val feedItemDetailsViewModel by androidLazy {
        getViewModel<FeedItemDetailsViewModel>(viewModelFactory)
    }

    private val sharedViewModel by androidLazy {
        sharedViewModel<SharedViewModel>(sharedViewModelFactory)
    }

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var groupLayoutManager: GridLayoutManager

    private lateinit var layoutFeedItemDetailsFragmentBinding: LayoutFeedItemDetailsFragmentBinding

    private var redditObject: RedditObject ?= null
    private var redditObjectData: RedditObjectData ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.home_nav_host_fragment
            duration = SHARED_ELEMENT_TRANSITION_DURATION
            isElevationShadowEnabled = true
        }
        super.onCreate(savedInstanceState)
        redditObject = arguments?.getParcelable(REDDIT_OBJECT_PARCELABLE_KEY)
        redditObjectData = redditObject?.data
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
        layoutFeedItemDetailsFragmentBinding.mlFeedDetails.transitionName = redditObjectData?.thumbnail
        redditObjectData?.name?.let { feedItemDetailsViewModel.fetchFeedByName(it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        redditObjectData?.subreddit_name_prefixed?.let { title ->
            (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        }
        if(redditObjectData == null) {
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
        redditObjectData?.permalink?.let {
            feedItemDetailsViewModel.fetchFeedItemDetails(it).observe(viewLifecycleOwner, { redditResponse ->
                when(redditResponse) {
                    is RedditResponse.Loading -> {
                        Timber.d("loading comments")
                        //show loader only if no comments are added
                        if(groupAdapter.groupCount <= 0)
                            layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible = true
                    }
                    is RedditResponse.Success<*> -> {
                        Timber.d("success fetch comments")
                        sharedViewModel.commentsLikeDisLikeMapping.forEach { entry ->
                            entry.value.removeObservers(viewLifecycleOwner)
                        }
                        if(layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible)
                            layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible = false
                        val groupsList = mutableListOf<ExpandableCommentGroup>()
                        (redditResponse.successData as Sequence<*>).forEach { commentsData ->
                            groupsList.add(ExpandableCommentGroup(commentsData as CommentsData, sharedViewModel = sharedViewModel, viewLifecycleOwner = viewLifecycleOwner))
                        }
                        groupAdapter.updateAsync(groupsList)
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
        feedItemDetailsViewModel.feedDetailsByNameLiveData.observe(viewLifecycleOwner, { redditObject ->
            redditObjectData = redditObject
            setLikeDislikeState()
            renderFeedDetailsImage()
            handleLikeDislikeClick()
            redditObject.subreddit_name_prefixed?.let { subReddit -> redditObject.author?.let { author ->
                setSubredditWithAuthorSpanned(subReddit, String.format(requireContext().getString(R.string.reddit_author_prefixed), author))
            } }
            layoutFeedItemDetailsFragmentBinding.ivDetailTitle.text = redditObject.title
            layoutFeedItemDetailsFragmentBinding.tvDomain.text = redditObject.getDomain()
            layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvUps.text = redditObject.ups?.getPrettyCount()
            layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvComments.text = redditObject.num_comments?.getPrettyCount()
            (layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvTime as MaterialTextView).text = PrettyTime(Locale.getDefault())
                .format(redditObject.created_utc?.times(1000L)?.let { Date(it) })

            handleDomainClick()
        })
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
            Glide.with(this)
                .load(source.url?.replace(FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.1f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(layoutFeedItemDetailsFragmentBinding.ivDetailImage)
        } ?: run {
            layoutFeedItemDetailsFragmentBinding.ivDetailImage.visibility = View.GONE
        }
    }

    private fun handleDomainClick() {
        redditObject?.data?.url_overridden_by_dest?.let { destinationURL ->
            layoutFeedItemDetailsFragmentBinding.llDomain.setOnClickListener {
                val intent = Intent(ACTION_VIEW)
                intent.data = Uri.parse(destinationURL)
                startActivity(intent)
            }
        }

    }

    private fun handleLikeDislikeClick() {
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbUp.setOnClickListener {
            redditObjectData?.deepCopy()?.let { it1 ->
                sharedViewModel.vote(CLICKED_LIKE, it1)
            }
        }
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbDown.setOnClickListener {
            redditObjectData?.deepCopy()?.let { it1 ->
                sharedViewModel.vote(CLICKED_DISLIKE, it1)
            }
        }
    }

    private fun setLikeDislikeState() {
        Timber.d("ups for: ${redditObjectData?.title}: ${redditObjectData?.ups}")
        when(redditObjectData?.likes) {
            true -> {
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbUp.isSelected = true
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbDown.isSelected = false
            }
            false -> {
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbUp.isSelected = false
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbDown.isSelected = true
            }
            else -> {
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbUp.isSelected = false
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbDown.isSelected = false
            }
        }
    }

    private fun setSubredditWithAuthorSpanned(author: String, user: String) {
        val bullet = "\u25CF"
        val combinedText = "$author $bullet $user"
        val spannable = SpannableString(combinedText)
        spannable.setSpan(
            TypefaceSpan("sans-serif"),
            0,
            author.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)),
            0,
            author.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            TypefaceSpan("sans-serif-light"),
            author.length + 1,
            combinedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        layoutFeedItemDetailsFragmentBinding.inclFeedInfo.tvSubredditAuthor.text = spannable
    }

    companion object {
        private const val SHARED_ELEMENT_TRANSITION_DURATION = 300L
    }
}