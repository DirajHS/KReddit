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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.LayoutFeedItemDetailsFragmentBinding
import com.diraj.kreddit.di.GlideApp
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
import com.google.android.material.transition.MaterialContainerTransform
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
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
    private var redditObjectDataWithoutReplies: RedditObjectData.RedditObjectDataWithoutReplies ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.home_nav_host_fragment
            duration = SHARED_ELEMENT_TRANSITION_DURATION
            isElevationShadowEnabled = true
        }
        super.onCreate(savedInstanceState)
        redditObject = arguments?.getParcelable(REDDIT_OBJECT_PARCELABLE_KEY)
        redditObjectDataWithoutReplies = redditObject?.data as RedditObjectData.RedditObjectDataWithoutReplies
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
        layoutFeedItemDetailsFragmentBinding.mlFeedDetails.transitionName = redditObjectDataWithoutReplies?.thumbnail
        redditObjectDataWithoutReplies?.name?.let { feedItemDetailsViewModel.fetchFeedByName(it) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        redditObjectDataWithoutReplies?.subredditNamePrefixed?.let { title ->
            (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        }
        if(redditObjectDataWithoutReplies == null) {
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
        redditObjectDataWithoutReplies?.permalink?.let { permaLink ->
            feedItemDetailsViewModel.fetchFeedItemDetails(permaLink)
            feedItemDetailsViewModel.feedDetailsLiveData.observe(viewLifecycleOwner, { redditResponse ->
                when(redditResponse) {
                    is RedditResponse.Loading -> {
                        Timber.d("loading comments")
                        //show loader only if no comments are added
                        if(groupAdapter.groupCount <= 0)
                            toggleCommentsFeedStatusVisibility(commentsVisibility = false, loadingVisibility = true, errorVisibility = false)
                    }
                    is RedditResponse.Success<*> -> {
                        handleCommentsFetchSuccess(redditResponse)
                    }
                    is RedditResponse.Error -> {
                        if(layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible)
                            layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible = false
                        handleCommentsFetchError(permaLink, redditResponse)
                    }
                }
            })
        }
    }

    private fun renderFeedDetails() {
        feedItemDetailsViewModel.feedDetailsByNameLiveData.observe(viewLifecycleOwner, { redditObject ->
            /*
            When we vote on a feed item and refresh, Reddit removes it from the feed (due its default settings), this would
            disturb the UI in tablets if it is already shown. This behaviour needs to be adjusted in user settings from Reddit page,
            however, from client side, we can make sure that we are updating details page with non-null data only.
             */
            redditObject?.let {
                redditObjectDataWithoutReplies = it as RedditObjectData.RedditObjectDataWithoutReplies
                setLikeDislikeState()
                renderFeedDetailsImage()
                handleLikeDislikeClick()
                redditObjectDataWithoutReplies?.subredditNamePrefixed?.let { subReddit -> redditObjectDataWithoutReplies?.author?.let { author ->
                    setSubredditWithAuthorSpanned(subReddit, String.format(requireContext().getString(R.string.reddit_author_prefixed), author))
                } }
                layoutFeedItemDetailsFragmentBinding.tvDetailTitle.text = redditObjectDataWithoutReplies?.title
                layoutFeedItemDetailsFragmentBinding.tvDomain.text = redditObjectDataWithoutReplies?.getDomain()
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvUps.text = redditObjectDataWithoutReplies?.ups?.getPrettyCount()
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvComments.text = redditObjectDataWithoutReplies?.numComments?.getPrettyCount()
                layoutFeedItemDetailsFragmentBinding.inclFeedActions.tvTime.text = PrettyTime(Locale.getDefault())
                    .format(redditObjectDataWithoutReplies?.createdUtc?.toLong()?.times(1000L)?.let { createdUtc -> Date(createdUtc) })

                handleDomainClick()
            }
        })
    }

    private fun renderFeedDetailsImage() {
        (redditObject?.data as? RedditObjectData.RedditObjectDataWithoutReplies)?.preview?.images?.first()?.source?.let { source ->
            layoutFeedItemDetailsFragmentBinding.ivDetailImage.visibility = View.VISIBLE
            ConstraintSet().apply {
                clone(layoutFeedItemDetailsFragmentBinding.clFeedDetails)
                layoutFeedItemDetailsFragmentBinding.ivDetailImage.id.let {
                    setDimensionRatio(it, "${source.width}:${source.height}")
                }
                applyTo(layoutFeedItemDetailsFragmentBinding.clFeedDetails)
            }
            GlideApp.with(this)
                .load(source.url?.replace(FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
                .thumbnail(0.1f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(layoutFeedItemDetailsFragmentBinding.ivDetailImage)
        } ?: run {
            layoutFeedItemDetailsFragmentBinding.ivDetailImage.visibility = View.GONE
        }
    }

    private fun handleDomainClick() {
        (redditObject?.data as? RedditObjectData.RedditObjectDataWithReplies)?.urlOverriddenByDest?.let { destinationURL ->
            layoutFeedItemDetailsFragmentBinding.llDomain.setOnClickListener {
                val intent = Intent(ACTION_VIEW)
                intent.data = Uri.parse(destinationURL)
                startActivity(intent)
            }
        }

    }

    private fun handleLikeDislikeClick() {
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbUp.setOnClickListener {
            redditObjectDataWithoutReplies?.deepCopy()?.let { it1 ->
                sharedViewModel.vote(CLICKED_LIKE, it1)
            }
        }
        layoutFeedItemDetailsFragmentBinding.inclFeedActions.ivThumbDown.setOnClickListener {
            redditObjectDataWithoutReplies?.deepCopy()?.let { it1 ->
                sharedViewModel.vote(CLICKED_DISLIKE, it1)
            }
        }
    }

    private fun setLikeDislikeState() {
        Timber.d("ups for: ${redditObjectDataWithoutReplies?.title}: ${redditObjectDataWithoutReplies?.ups}")
        when(redditObjectDataWithoutReplies?.likes) {
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

    private fun handleCommentsFetchSuccess(redditResponse: RedditResponse.Success<*>) {
        Timber.d("success fetch comments")
        sharedViewModel.commentsLikeDisLikeMapping.forEach { entry ->
            entry.value.removeObservers(viewLifecycleOwner)
        }
        toggleCommentsFeedStatusVisibility(commentsVisibility = true, loadingVisibility = false, errorVisibility = false)
        val groupsList = mutableListOf<ExpandableCommentGroup>()
        (redditResponse.successData as Sequence<*>).forEach { commentsData ->
            groupsList.add(ExpandableCommentGroup(commentsData as CommentsData,
                sharedViewModel = sharedViewModel, viewLifecycleOwner = viewLifecycleOwner))
        }
        groupAdapter.updateAsync(groupsList)
    }

    private fun handleCommentsFetchError(permaLink: String, redditResponse: RedditResponse.Error) {
        when(redditResponse.ex) {
            is HttpException -> {
                layoutFeedItemDetailsFragmentBinding.errorView.tvError.text = redditResponse.ex.message()
            }
            is IOException -> {
                layoutFeedItemDetailsFragmentBinding.errorView.tvError.text = getString(R.string.generic_error_string)
            }
        }
        toggleCommentsFeedStatusVisibility(commentsVisibility = false, loadingVisibility = false, errorVisibility = true)
        layoutFeedItemDetailsFragmentBinding.errorView.root.setOnClickListener {
            feedItemDetailsViewModel.fetchFeedItemDetails(permaLink)
        }
    }

    private fun toggleCommentsFeedStatusVisibility(commentsVisibility: Boolean, loadingVisibility: Boolean, errorVisibility: Boolean) {
        layoutFeedItemDetailsFragmentBinding.rvFeedItemComments.isVisible = commentsVisibility
        layoutFeedItemDetailsFragmentBinding.loadingView.root.isVisible = loadingVisibility
        layoutFeedItemDetailsFragmentBinding.errorView.root.isVisible = errorVisibility
    }

    companion object {
        private const val SHARED_ELEMENT_TRANSITION_DURATION = 300L
    }
}