package com.diraj.kreddit.presentation.home.epoxy.models

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnNextLayout
import com.airbnb.epoxy.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diraj.kreddit.R
import com.diraj.kreddit.data.models.PreviewImage
import com.diraj.kreddit.data.models.RedditObjectDataWithoutReplies
import com.diraj.kreddit.data.models.Resolutions
import com.diraj.kreddit.databinding.FeedListItemBinding
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener
import com.diraj.kreddit.utils.KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY
import com.diraj.kreddit.utils.fromHtml
import com.diraj.kreddit.utils.getPrettyCount
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FeedEpoxyView @JvmOverloads constructor(
    c: Context,
    a: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(c, a, defStyleAttr) {

    @ModelProp
    lateinit var redditObject: RedditObjectDataWithoutReplies

    var feedItemClickListener: IFeedClickListener ?= null
        @CallbackProp set

    @ModelProp(ModelProp.Option.DoNotHash)
    lateinit var glideRequestManager: RequestManager

    private val feedListItemBinding: FeedListItemBinding =
        FeedListItemBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var defaultImageViewConstraintSet: ConstraintSet

    @AfterPropsSet
    fun renderFeedItem() {
        feedListItemBinding.tvTitle.text = redditObject.title?.fromHtml()
        redditObject.subredditNamePrefixed?.let { subReddit -> redditObject.author?.let { author ->
            setSubredditWithAuthorSpanned(subReddit, String.format(context.getString(R.string.reddit_author_prefixed), author))
        } }
        feedListItemBinding.inclFeedActions.tvTime.text = PrettyTime(Locale.getDefault())
            .format(redditObject.createdUtc?.toLong()?.times(1000L)?.let { Date(it) })

        redditObject.preview?.images?.first()?.source?.let { source ->
            redditObject.preview?.images?.first()?.resolutions
                ?.let { resolutions -> processPreview(resolutions, source) }
        } ?: run {
            feedListItemBinding.ivFeedImage.visibility = View.GONE
        }

        //For shared element transition
        ViewCompat.setTransitionName(feedListItemBinding.cvFeedItem, redditObject.thumbnail)
        feedListItemBinding.root.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(it, redditObject)
        }

        setLikeDislikeState()
        setLikeDislikeClickListener()
    }

    @OnViewRecycled
    fun onViewRecycled() {
        if(::defaultImageViewConstraintSet.isInitialized)
            defaultImageViewConstraintSet.applyTo(feedListItemBinding.clFeedItem)
    }

    private fun processPreview(resolutions: List<Resolutions>, defaultSource: PreviewImage) {
        feedListItemBinding.ivFeedImage.visibility = View.VISIBLE
        /*
        The general idea here is to reuse the recycled views as efficiently as possible. So, if the
        view was rendered for any previous item, the width would be available, so we can go ahead
        and draw the preview with closest resolution, otherwise we draw when the view is drawn.
        This way we can load image as per the available resolution optimizing memory usage.
         */
        Timber.d("measured width: ${feedListItemBinding.ivFeedImage.measuredWidth}")
        if (feedListItemBinding.ivFeedImage.measuredWidth > 0) {
            Timber.d("view is recycled, so set the image directly")
            defaultImageViewConstraintSet = ConstraintSet()
            defaultImageViewConstraintSet.clone(feedListItemBinding.clFeedItem)
            val defaultImageViewWidth = feedListItemBinding.ivFeedImage.measuredWidth
            renderThumbnail(defaultSource, resolutions, defaultImageViewWidth)
        } else {
            feedListItemBinding.ivFeedImage.doOnNextLayout {
                defaultImageViewConstraintSet = ConstraintSet()
                defaultImageViewConstraintSet.clone(feedListItemBinding.clFeedItem)
                val defaultImageViewWidth = feedListItemBinding.ivFeedImage.measuredWidth
                renderThumbnail(defaultSource, resolutions, defaultImageViewWidth)
            }
        }

    }

    private fun renderThumbnail(defaultSource: PreviewImage, resolutions: List<Resolutions>,
                                defaultImageViewWidth: Int) {
        var selectedSource = defaultSource
        Timber.d("max default image view width: $defaultImageViewWidth")
        Timber.d("default : ${defaultSource.width}x${defaultSource.height}")
        for(resolution in resolutions) {
            if(resolution.width <= defaultImageViewWidth) {
                selectedSource = PreviewImage(url = resolution.url, width = resolution.width,
                    height = resolution.height)
                Timber.d("updating source to : ${selectedSource.width}x${selectedSource.height}")
            } else {
                Timber.e("discarding resolution: ${resolution.width}x${resolution.height}")
            }
        }
        ConstraintSet().apply {
            clone(feedListItemBinding.clFeedItem)
            feedListItemBinding.ivFeedImage.id.let {
                setDimensionRatio(it, "${selectedSource.width}:${selectedSource.height}")
            }
            applyTo(feedListItemBinding.clFeedItem)
        }
        glideRequestManager
            .load(selectedSource.url?.replace(FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(feedListItemBinding.ivFeedImage)
    }


    private fun setLikeDislikeState() {
        feedListItemBinding.apply {
            inclFeedActions.tvUps.text = redditObject.ups?.getPrettyCount()
            inclFeedActions.tvComments.text = redditObject.numComments?.getPrettyCount()
        }
        feedListItemBinding.inclFeedActions.apply {
            ivThumbUp.isSelected = redditObject.likes == true
            ivThumbDown.isSelected = redditObject.likes == false
            tvUps.setTextColor(ContextCompat.getColor(context, redditObject.likes?.let { likes ->
                if(likes) R.color.like_true_color else R.color.like_false_color
            } ?: R.color.feed_title_color))
        }
    }

    private fun setLikeDislikeClickListener() {
        feedListItemBinding.inclFeedActions.ivThumbUp.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(
                feedListItemBinding.inclFeedActions.ivThumbUp,
                redditObject
            )
        }
        feedListItemBinding.inclFeedActions.ivThumbDown.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(
                feedListItemBinding.inclFeedActions.ivThumbDown,
                redditObject
            )
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
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimaryDark)),
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
        feedListItemBinding.inclFeedInfo.tvSubredditAuthor.text = spannable
    }

}
