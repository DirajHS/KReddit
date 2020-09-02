package com.diraj.kreddit.presentation.home.epoxy.models

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.diraj.kreddit.databinding.FeedListItemBinding
import com.diraj.kreddit.network.models.RedditObject
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

    private val feedListItemBinding: FeedListItemBinding =
        FeedListItemBinding.inflate(LayoutInflater.from(context), this, true)

    @AfterPropsSet
    fun renderFeedItem() {
        feedListItemBinding.tvTitle.text = redditObject.data.title
        feedListItemBinding.tvSubreddit.text = redditObject.data.subreddit_name_prefixed
        feedListItemBinding.tvSender.text = redditObject.data.author

        feedListItemBinding.tvTime.text = PrettyTime(Locale.getDefault())
            .format(Date(redditObject.data.created_utc*1000L))

        redditObject.data.preview?.images?.first()?.source?.let { source ->
            feedListItemBinding.ivFeedImage.visibility = View.VISIBLE
            ConstraintSet().apply {
                clone(feedListItemBinding.clFeedItem)
                feedListItemBinding.ivFeedImage.id.let {
                    setDimensionRatio(it, "${source.width}:${source.height}")
                }
                applyTo(feedListItemBinding.clFeedItem)
            }
            Glide.with(context)
                .load(source.url?.replace("amp;", ""))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .thumbnail(0.1f)
                .into(feedListItemBinding.ivFeedImage)
        } ?: run {
            feedListItemBinding.ivFeedImage.visibility = View.GONE
        }

        feedListItemBinding.tvUps.text = redditObject.data.ups.getPrettyCount()
        feedListItemBinding.tvComments.text = redditObject.data.num_comments.getPrettyCount()
    }

}