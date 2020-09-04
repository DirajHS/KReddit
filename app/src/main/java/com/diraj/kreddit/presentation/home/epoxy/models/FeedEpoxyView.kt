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
import com.diraj.kreddit.network.models.RedditObject
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener
import com.diraj.kreddit.utils.KRedditConstants.FEED_THUMBNAIL_URL_REPLACEMENT_KEY
import com.diraj.kreddit.utils.getPrettyCount
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FeedEpoxyView @JvmOverloads constructor(
    c : Context,
    a : AttributeSet? = null,
    defStyleAttr : Int = 0
) : CardView(c, a, defStyleAttr) {

    @ModelProp
    lateinit var redditObject: RedditObject

    var feedItemClickListener: IFeedClickListener ?= null
        @CallbackProp set

    private val feedListItemBinding: FeedListItemBinding =
        FeedListItemBinding.inflate(LayoutInflater.from(context), this, true)

    @AfterPropsSet
    fun renderFeedItem() {
        feedListItemBinding.tvTitle.text = redditObject.data.title
        feedListItemBinding.inclFeedInfo.tvSubreddit.text = redditObject.data.subreddit_name_prefixed
        feedListItemBinding.inclFeedInfo.tvDomain.text = redditObject.data.getDomain()
        feedListItemBinding.inclFeedInfo.tvAuthor.text = redditObject.data.author

        feedListItemBinding.inclFeedActions.tvTime.text = PrettyTime(Locale.getDefault())
            .format(redditObject.data.created_utc?.times(1000L)?.let { Date(it) })

        redditObject.data.preview?.images?.first()?.source?.let { source ->
            renderThumbnail(source)
        } ?: run {
            feedListItemBinding.ivFeedImage.visibility = View.GONE
        }

        feedListItemBinding.inclFeedActions.tvUps.text = redditObject.data.ups?.getPrettyCount()
        feedListItemBinding.inclFeedActions.tvComments.text = redditObject.data.num_comments?.getPrettyCount()

        ViewCompat.setTransitionName(feedListItemBinding.cvFeedItem, redditObject.data.thumbnail)
        feedListItemBinding.root.setOnClickListener {
            feedItemClickListener?.onFeedItemClicked(it, redditObject)
        }
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

}