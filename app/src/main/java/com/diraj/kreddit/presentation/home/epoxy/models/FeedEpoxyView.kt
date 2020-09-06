package com.diraj.kreddit.presentation.home.epoxy.models

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diraj.kreddit.databinding.FeedListItemBinding
import com.diraj.kreddit.network.models.PreviewImage
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener
import com.diraj.kreddit.utils.KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY
import com.diraj.kreddit.utils.getPrettyCount
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.util.*

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FeedEpoxyView @JvmOverloads constructor(
    c : Context,
    a : AttributeSet? = null,
    defStyleAttr : Int = 0
) : CardView(c, a, defStyleAttr) {

    @ModelProp
    lateinit var redditObject: RedditObjectData

    var feedItemClickListener: IFeedClickListener ?= null
        @CallbackProp set

    private val feedListItemBinding: FeedListItemBinding =
        FeedListItemBinding.inflate(LayoutInflater.from(context), this, true)

    @AfterPropsSet
    fun renderFeedItem() {
        feedListItemBinding.tvTitle.text = redditObject.title
        feedListItemBinding.inclFeedInfo.tvSubreddit.text = redditObject.subreddit_name_prefixed
        feedListItemBinding.inclFeedInfo.tvDomain.text = redditObject.getDomain()
        feedListItemBinding.inclFeedInfo.tvAuthor.text = redditObject.author

        feedListItemBinding.inclFeedActions.tvTime.text = PrettyTime(Locale.getDefault())
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
        Glide.with(context)
            .load(source.url?.replace(FEED_THUMBNAIL_URL_REPLACEMENT_KEY, ""))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .thumbnail(0.1f)
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
            }
            false -> {
                feedListItemBinding.inclFeedActions.ivThumbUp.isSelected = false
                feedListItemBinding.inclFeedActions.ivThumbDown.isSelected = true
            }
            else -> {
                feedListItemBinding.inclFeedActions.ivThumbUp.isSelected = false
                feedListItemBinding.inclFeedActions.ivThumbDown.isSelected = false
            }
        }
    }

    private fun setLikeDislikeClickListener() {
        feedListItemBinding.inclFeedActions.ivThumbUp.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(feedListItemBinding.inclFeedActions.ivThumbUp, redditObject)
        }
        feedListItemBinding.inclFeedActions.ivThumbDown.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(feedListItemBinding.inclFeedActions.ivThumbDown, redditObject)
        }
    }

}