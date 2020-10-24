package com.diraj.kreddit.presentation.home.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.diraj.kreddit.databinding.FeedItemNetworkStateBinding
import com.diraj.kreddit.presentation.home.recyclerview.viewholder.FeedItemNetworkStateViewHolder

class PostsLoadStateAdapter(private val postsAdapter: PostsAdapter)
    : LoadStateAdapter<FeedItemNetworkStateViewHolder>() {

    override fun onBindViewHolder(holder: FeedItemNetworkStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): FeedItemNetworkStateViewHolder {
        val feedItemNetworkStateBinding = FeedItemNetworkStateBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedItemNetworkStateViewHolder(feedItemNetworkStateBinding) { postsAdapter.retry() }
    }
}