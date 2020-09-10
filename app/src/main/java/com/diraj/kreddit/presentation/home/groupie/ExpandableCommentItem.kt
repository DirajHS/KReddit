package com.diraj.kreddit.presentation.home.groupie

import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.diraj.kreddit.R
import com.diraj.kreddit.databinding.ItemExpandableCommentBinding
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.diraj.kreddit.utils.KRedditConstants
import com.diraj.kreddit.utils.fromHtml
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
        val context = expandableCommentBinding.root.context
        addingDepthViews(viewHolder)

        expandableCommentBinding.tvAuthor.text = String.format(context.getString(R.string.reddit_author_prefixed), comment.author)
        expandableCommentBinding.tvComment.text = comment.body?.fromHtml()
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
        setReplyClickListener()
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
                expandableCommentBinding.btnMoreReplies.icon = ContextCompat
                    .getDrawable(expandableCommentBinding.root.context, R.drawable.ic_arrow_down)
                expandableCommentBinding.tvMoreReplies.text = expandableCommentBinding.root.context.getString(R.string.show_replies)
            } else {
                expandableCommentBinding.btnMoreReplies.icon = ContextCompat
                    .getDrawable(expandableCommentBinding.root.context, R.drawable.ic_arrow_up)
                expandableCommentBinding.tvMoreReplies.text = expandableCommentBinding.root.context.getString(R.string.hide_replies)
            }
        } else {
            expandableCommentBinding.btnMoreReplies.isVisible = false
            expandableCommentBinding.tvMoreReplies.isVisible = false
        }
    }

    private fun setLikeDislikeState(commentsData: CommentsData) {
        Timber.d("ups for: ${commentsData.author}: ${commentsData.ups} ${commentsData.likes}")
        expandableCommentBinding.tvVotes.text = commentsData.ups?.getPrettyCount()
        val context = expandableCommentBinding.root.context
        when(commentsData.likes) {
            true -> {
                expandableCommentBinding.btnUpvote.isSelected = true
                expandableCommentBinding.btnDownVote.isSelected = false
                expandableCommentBinding.tvVotes.setTextColor(ContextCompat.getColor(context, R.color.like_true_color))
            }
            false -> {
                expandableCommentBinding.btnUpvote.isSelected = false
                expandableCommentBinding.btnDownVote.isSelected = true
                expandableCommentBinding.tvVotes.setTextColor(ContextCompat.getColor(context, R.color.like_false_color))
            }
            else -> {
                expandableCommentBinding.btnUpvote.isSelected = false
                expandableCommentBinding.btnDownVote.isSelected = false
                expandableCommentBinding.tvVotes.setTextColor(ContextCompat.getColor(context, R.color.feed_title_color))
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

    private fun setReplyClickListener() {
        val context = expandableCommentBinding.root.context
        expandableCommentBinding.tvReply.setOnClickListener {
            Toast.makeText(context, context.getString(R.string.feature_yet_to_come), Toast.LENGTH_SHORT).show()
        }
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return comment.name == (other as ExpandableCommentItem).comment.name
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return comment == (other as ExpandableCommentItem).comment
    }

}
