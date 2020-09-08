package com.diraj.kreddit.presentation.home.groupie

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.ItemExpandableCommentBinding
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.utils.KRedditConstants
import com.diraj.kreddit.utils.getPrettyCount
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import timber.log.Timber

class ExpandableCommentItem constructor(
    private val comment: CommentsData,
    private val depth: Int,
    private val sharedViewModel: SharedViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : Item<GroupieViewHolder>(), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup
    private lateinit var expandableCommentBinding: ItemExpandableCommentBinding

    private val commentsLikesDislikesObserver = Observer<CommentsData> {
        if(comment.name == it.name) {
            setLikeDislikeState(it)
        }
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        expandableCommentBinding = ItemExpandableCommentBinding.bind(viewHolder.itemView)
        addingDepthViews(viewHolder)

        expandableCommentBinding.tvAuthor.text = comment.author
        expandableCommentBinding.tvComment.text = fromHtml(comment.body)
        expandableCommentBinding.tvVotes.text = comment.ups?.getPrettyCount()
        toggleRepliesText()
        viewHolder.itemView.apply {
            setOnClickListener {
                expandableGroup.onToggleExpanded()
                toggleRepliesText()
            }
        }
        setLikeDislikeClickListener()
        setLikeDislikeState(comment)
        sharedViewModel.commentsLikeDisLikeMapping[comment.name]?.observe(viewLifecycleOwner, commentsLikesDislikesObserver)
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

    override fun getLayout(): Int {
        return R.layout.item_expandable_comment
    }

    override fun unbind(viewHolder: GroupieViewHolder) {
        super.unbind(viewHolder)
        Timber.d("unbinding: ${comment.author}")
        sharedViewModel.commentsLikeDisLikeMapping[comment.name]?.removeObserver(commentsLikesDislikesObserver)
    }

    private fun addingDepthViews(viewHolder: GroupieViewHolder) {
        expandableCommentBinding.llSeparatorContainer.removeAllViews()
        expandableCommentBinding.llSeparatorContainer.visibility =
            if (depth > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
        for (i in 1..depth) {
            val v : View = LayoutInflater.from(viewHolder.itemView.context)
                .inflate(
                    R.layout.layout_separator_view,
                    expandableCommentBinding.llSeparatorContainer,
                    false
                )
            expandableCommentBinding.llSeparatorContainer.addView(v)
        }
        expandableCommentBinding.tvComment.requestLayout()
    }

    private fun toggleRepliesText() {
        if(expandableGroup.childCount > 0) {
            expandableCommentBinding.btnMoreReplies.isVisible = true
            expandableCommentBinding.tvMoreReplies.isVisible = true
            if(!expandableGroup.isExpanded) {
                expandableCommentBinding.btnMoreReplies.background = ContextCompat
                    .getDrawable(expandableCommentBinding.root.context, R.drawable.ic_arrow_down)
                expandableCommentBinding.tvMoreReplies.text = expandableCommentBinding.root.context.getString(R.string.show_replies)
            } else {
                expandableCommentBinding.btnMoreReplies.background = ContextCompat
                    .getDrawable(expandableCommentBinding.root.context, R.drawable.ic_arrow_up)
                expandableCommentBinding.tvMoreReplies.text = expandableCommentBinding.root.context.getString(R.string.hide_replies)
            }
        } else {
            expandableCommentBinding.btnMoreReplies.isVisible = false
            expandableCommentBinding.tvMoreReplies.isVisible = false
        }
    }

    @Suppress("DEPRECATION")
    private fun fromHtml(source: String?): Spanned? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(source)
        }
    }

    private fun setLikeDislikeState(commentsData: CommentsData) {
        Timber.d("ups for: ${commentsData.author}: ${commentsData.ups} ${commentsData.likes}")
        expandableCommentBinding.tvVotes.text = commentsData.ups?.getPrettyCount()
        when(commentsData.likes) {
            true -> {
                expandableCommentBinding.btnUpvote.isSelected = true
                expandableCommentBinding.btnDownVote.isSelected = false
            }
            false -> {
                expandableCommentBinding.btnUpvote.isSelected = false
                expandableCommentBinding.btnDownVote.isSelected = true
            }
            else -> {
                expandableCommentBinding.btnUpvote.isSelected = false
                expandableCommentBinding.btnDownVote.isSelected = false
            }
        }
    }

    private fun setLikeDislikeClickListener() {
        expandableCommentBinding.btnUpvote.setOnClickListener {
            Timber.d("clicked like")
            sharedViewModel.voteComment(KRedditConstants.CLICKED_LIKE, comment).observe(viewLifecycleOwner, commentsLikesDislikesObserver)
        }
        expandableCommentBinding.btnDownVote.setOnClickListener {
            Timber.d("clicked dislike")
            sharedViewModel.voteComment(KRedditConstants.CLICKED_DISLIKE, comment).observe(viewLifecycleOwner, commentsLikesDislikesObserver)
        }
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return comment.name == (other as ExpandableCommentItem).comment.name
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return comment == (other as ExpandableCommentItem).comment
    }

}
