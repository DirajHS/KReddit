package com.diraj.kreddit.presentation.home.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.diraj.kreddit.databinding.FeedListItemBinding
import com.diraj.kreddit.network.models.RedditObjectData
import com.diraj.kreddit.presentation.home.fragment.IFeedClickListener
import com.diraj.kreddit.presentation.home.recyclerview.viewholder.FeedItemViewHolder

class PostsAdapter(private val glideRequestManager: RequestManager,
                   private val feedClickListener: IFeedClickListener)
    : PagingDataAdapter<RedditObjectData.RedditObjectDataWithoutReplies, RecyclerView.ViewHolder>(
    POST_COMPARATOR) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { (holder as FeedItemViewHolder).bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val feedItemViewBinding = FeedListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedItemViewHolder(feedItemViewBinding, feedClickListener, glideRequestManager)
    }

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<RedditObjectData.RedditObjectDataWithoutReplies>() {
            override fun areContentsTheSame(oldItem: RedditObjectData.RedditObjectDataWithoutReplies,
                                            newItem: RedditObjectData.RedditObjectDataWithoutReplies)
                    : Boolean {
                return oldItem.name == newItem.name
            }

            override fun areItemsTheSame(oldItem: RedditObjectData.RedditObjectDataWithoutReplies,
                                         newItem: RedditObjectData.RedditObjectDataWithoutReplies)
                    : Boolean {
                return oldItem.name == newItem.name
            }
        }
    }
}