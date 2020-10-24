package com.diraj.kreddit.presentation.home.recyclerview.viewholder

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.diraj.kreddit.databinding.FeedItemNetworkStateBinding

class FeedItemNetworkStateViewHolder(private val feedItemNetworkStateBinding: FeedItemNetworkStateBinding,
                                     private val retryCallback: () -> Unit)
    : RecyclerView.ViewHolder(feedItemNetworkStateBinding.root) {

    init {
        feedItemNetworkStateBinding.errorView.root.setOnClickListener {
            retryCallback()
        }
    }
    fun bind(loadState: LoadState) {
        feedItemNetworkStateBinding.loadingView.root.isVisible = loadState is LoadState.Loading
        feedItemNetworkStateBinding.errorView.root.isVisible = loadState is LoadState.Error
    }

}