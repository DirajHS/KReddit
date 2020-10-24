package com.diraj.kreddit.presentation.home.recyclerview.viewholder

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.FeedListItemBinding
import com.diraj.kreddit.network.models.PreviewImage
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.network.models.Resolutions
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener
import com.diraj.kreddit.utils.KRedditConstants
import com.diraj.kreddit.utils.fromHtml
import com.diraj.kreddit.utils.getPrettyCount
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*

class FeedItemViewHolder(private val feedListItemBinding: FeedListItemBinding,
                         private val feedItemClickListener: IFeedClickListener,
                         private val glideRequestManager: RequestManager
)
    : RecyclerView.ViewHolder(feedListItemBinding.root) {

    private lateinit var defaultImageViewConstraintSet: ConstraintSet

    private val context = feedListItemBinding.root.context

    fun bind(redditObject: RedditObjectData.RedditObjectDataWithoutReplies) {
        feedListItemBinding.tvTitle.text = redditObject.title?.fromHtml()
        redditObject.subredditNamePrefixed?.let { subReddit -> redditObject.author?.let { author ->
            setSubredditWithAuthorSpanned(subReddit, String.format(context.getString(R.string.reddit_author_prefixed), author))
        } }
        feedListItemBinding.inclFeedActions.tvTime.text = PrettyTime(Locale.getDefault())
            .format(redditObject.createdUtc?.toLong()?.times(1000L)?.let { Date(it) })

        redditObject.preview?.images?.first()?.source?.let { source ->
            redditObject.preview.images.first().resolutions
                ?.let { resolutions -> processPreview(resolutions, source) }
        } ?: run {
            feedListItemBinding.ivFeedImage.visibility = View.GONE
        }

        //For shared element transition
        ViewCompat.setTransitionName(feedListItemBinding.cvFeedItem, redditObject.thumbnail)
        feedListItemBinding.root.setOnClickListener {
            feedItemClickListener.onFeedItemClicked(it, redditObject)
        }

        setLikeDislikeState(redditObject)
        setLikeDislikeClickListener(redditObject)
    }

    fun unbind() {
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

    private fun renderThumbnail(defaultSource: PreviewImage, resolutions: List<Resolutions>, defaultImageViewWidth: Int) {
        var selectedSource = defaultSource
        Timber.d("max default image view width: $defaultImageViewWidth")
        Timber.d("default : ${defaultSource.width}x${defaultSource.height}")
        for(resolution in resolutions) {
            if(resolution.width <= defaultImageViewWidth) {
                selectedSource = PreviewImage(url = resolution.url, width = resolution.width, height = resolution.height)
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
            .load(selectedSource.url?.replace(KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
            .thumbnail(0.1f)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(feedListItemBinding.ivFeedImage)
    }


    private fun setLikeDislikeState(redditObject: RedditObjectData.RedditObjectDataWithoutReplies) {
        feedListItemBinding.inclFeedActions.tvUps.text = redditObject.ups?.getPrettyCount()
        feedListItemBinding.inclFeedActions.tvComments.text = redditObject.numComments?.getPrettyCount()
        when(redditObject.likes) {
            true -> {
                feedListItemBinding.inclFeedActions.ivThumbUp.isSelected = true
                feedListItemBinding.inclFeedActions.ivThumbDown.isSelected = false
                feedListItemBinding.inclFeedActions.tvUps.setTextColor(ContextCompat.getColor(context, R.color.like_true_color))
            }
            false -> {
                feedListItemBinding.inclFeedActions.ivThumbUp.isSelected = false
                feedListItemBinding.inclFeedActions.ivThumbDown.isSelected = true
                feedListItemBinding.inclFeedActions.tvUps.setTextColor(ContextCompat.getColor(context, R.color.like_false_color))
            }
            else -> {
                feedListItemBinding.inclFeedActions.ivThumbUp.isSelected = false
                feedListItemBinding.inclFeedActions.ivThumbDown.isSelected = false
                feedListItemBinding.inclFeedActions.tvUps.setTextColor(ContextCompat.getColor(context, R.color.feed_title_color))
            }
        }
    }

    private fun setLikeDislikeClickListener(redditObject: RedditObjectData.RedditObjectDataWithoutReplies) {
        feedListItemBinding.inclFeedActions.ivThumbUp.setOnClickListener {
            feedItemClickListener.onFeedItemClicked(
                feedListItemBinding.inclFeedActions.ivThumbUp,
                redditObject
            )
        }
        feedListItemBinding.inclFeedActions.ivThumbDown.setOnClickListener {
            feedItemClickListener.onFeedItemClicked(
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