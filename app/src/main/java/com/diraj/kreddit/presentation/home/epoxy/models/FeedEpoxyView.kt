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
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.FeedListItemBinding
import com.diraj.kreddit.network.models.PreviewImage
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener
import com.diraj.kreddit.utils.KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY
import com.diraj.kreddit.utils.fromHtml
import com.diraj.kreddit.utils.getPrettyCount
import com.google.android.material.textview.MaterialTextView
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
    lateinit var redditObject: RedditObjectData

    var feedItemClickListener: IFeedClickListener ?= null
        @CallbackProp set

    @ModelProp(ModelProp.Option.DoNotHash)
    lateinit var glideRequestManager: RequestManager

    private val feedListItemBinding: FeedListItemBinding =
        FeedListItemBinding.inflate(LayoutInflater.from(context), this, true)

    @AfterPropsSet
    fun renderFeedItem() {
        feedListItemBinding.tvTitle.text = redditObject.title?.fromHtml()
        redditObject.subreddit_name_prefixed?.let { subReddit -> redditObject.author?.let { author ->
            setSubredditWithAuthorSpanned(subReddit, String.format(context.getString(R.string.reddit_author_prefixed), author))
        } }
        (feedListItemBinding.inclFeedActions.tvTime as MaterialTextView).text = PrettyTime(Locale.getDefault())
            .format(redditObject.created_utc?.times(1000L)?.let { Date(it) })

        redditObject.preview?.images?.first()?.source?.let { source ->
            renderThumbnail(source)
        } ?: run {
            feedListItemBinding.ivFeedImage.visibility = View.GONE
        }

        ViewCompat.setTransitionName(feedListItemBinding.cvFeedItem, redditObject.thumbnail)
        feedListItemBinding.root.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(it, redditObject)
        }

        setLikeDislikeState()
        setLikeDislikeClickListener()
    }

    private fun renderThumbnail(source: PreviewImage) {
        feedListItemBinding.ivFeedImage.visibility = View.VISIBLE
        ConstraintSet().apply {
            clone(feedListItemBinding.clFeedItem)
            feedListItemBinding.ivFeedImage.id.let {
                setDimensionRatio(it, "${source.width}:${source.height}")
            }
            applyTo(feedListItemBinding.clFeedItem)
        }
        glideRequestManager
            .load(source.url?.replace(FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .thumbnail(0.1f)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(feedListItemBinding.ivFeedImage)
    }

    private fun setLikeDislikeState() {
        Timber.d("ups for: ${redditObject.title}: ${redditObject.ups}")
        feedListItemBinding.inclFeedActions.tvUps.text = redditObject.ups?.getPrettyCount()
        feedListItemBinding.inclFeedActions.tvComments.text = redditObject.num_comments?.getPrettyCount()
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